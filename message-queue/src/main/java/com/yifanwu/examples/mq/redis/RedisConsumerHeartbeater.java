package com.yifanwu.examples.mq.redis;

import com.yifanwu.examples.mq.SiQueue;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * it is actually a inner class of {@link RedisSingleMsgConsumer}. Just think top level class is better organized.
 * There are some duplicted fields which should be OK because it is supposed to have very few instances.
 * @author Yifan.Wu on 7/13/2018
 */
class RedisConsumerHeartbeater implements AutoCloseable {
    private static final Logger log = getLogger(RedisConsumerHeartbeater.class);

    public static final String SCRIPT_PATH_HEARTBEATER_DEFAULT = "scripts/redis/mq/consumer-heartbeater.lua";

    public static final String HEARTBEATER_LEADER_PREFIX = "LEADER";
    public static final String HEARTBEATER_LIST_PREFIX = "LIST";
    public static final String HEARTBEATER_HB_POSTFIX = "HB";

    private final StringRedisTemplate stringRedisTemplate;
    private final String leaderHeartbeaterKey;    // leader key: LEADER:WAIT_QUEUE_NAME
    private final String heartbeaterValue;        // compete for leader; in list; processing Q name   :  NAME:UUID
    private final String heartbeaterListKey;      // list of all Consumers key   :   LIST:WAIT_QUEUE_NAME
    private final String heartbeaterHb;           // heartbeat key and value      :  NAME:UUID:HB
    private final RedisScript<List> heartbeaterScript;
    private final SiQueue waitQueue;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture cancellableFuture;

    private int initialDelaySec = 20;
    private int delaySec = 30;

    private int heartbeatExpirySec = 120;
    private int listExpiryFactor = 30;

    RedisConsumerHeartbeater(StringRedisTemplate stringRedisTemplate, SiQueue waitQueue, SiQueue processQueue) throws IOException {
        this.waitQueue = waitQueue;
        this.stringRedisTemplate = stringRedisTemplate;
        leaderHeartbeaterKey = HEARTBEATER_LEADER_PREFIX + RedisQueue.KEY_JOINER + waitQueue.getQueueName();
        heartbeaterValue = processQueue.getQueueName();
        heartbeaterListKey = HEARTBEATER_LIST_PREFIX + RedisQueue.KEY_JOINER + waitQueue.getQueueName();
        heartbeaterHb = heartbeaterValue + RedisQueue.KEY_JOINER + HEARTBEATER_HB_POSTFIX;

        String script = new ResourceScriptSource(new ClassPathResource(SCRIPT_PATH_HEARTBEATER_DEFAULT)).getScriptAsString();
        heartbeaterScript = RedisScript.of(script, List.class);
    }

    void register() {
        // 3 trips to Redis, OK for start up time
        // send heartbeat
        stringRedisTemplate.opsForValue().set(heartbeaterHb, heartbeaterHb, heartbeatExpirySec, TimeUnit.SECONDS);
        // register itself in the list
        long size = stringRedisTemplate.opsForList().leftPush(heartbeaterListKey, heartbeaterValue);
        log.info("registered list size: {}", size);
        // expire the list some huge time later
        boolean res = stringRedisTemplate.expire(heartbeaterListKey, heartbeatExpirySec * listExpiryFactor, TimeUnit.SECONDS);
        if (!res) {
            throw new IllegalStateException("Unable to set expiry time on key=" + heartbeaterListKey);
        }
    }

    void init() {
        register();
        Runnable task = () -> {
            if (log.isDebugEnabled()) {
                log.debug("Consumer {} heartbeating started.", heartbeaterValue);
            }
            // 1. Refresh own heartbeat at heartbeaterHb and refresh list
            // 2. Compete for leader
            // 3. depends on #2 result:
            //    a. if is leader, get the list
            //       iterate the list and find out who has no heartbeat
            //       reQueue the message whose owner lost heartbeat
            //    b. if not leader, return
            List result = stringRedisTemplate.execute(heartbeaterScript,
                    Arrays.asList(heartbeaterHb,  //key1
                            heartbeaterListKey,   // key2
                            leaderHeartbeaterKey,  // key3
                            waitQueue.getQueueName()   // key4
                    ),
                    heartbeaterHb,   // arg1
                    String.valueOf(heartbeatExpirySec),  // arg2
                    String.valueOf(heartbeatExpirySec * listExpiryFactor), // arg3
                    heartbeaterValue,  //arg4
                    String.valueOf(heartbeatExpirySec),  //arg5
                    RedisQueue.KEY_JOINER + HEARTBEATER_HB_POSTFIX   //arg6
            );
            if (Objects.nonNull(result.get(0))) {
                if (((List)result.get(1)).size() > 0) {
                    log.warn("Leader={} found original listSize={} and orphaned consumers {}", heartbeaterValue, result.get(2), result.get(1));
                } else {
                    log.debug("No orphaned consumer found by the leader={} and listSize={}", heartbeaterValue, result.get(2));
                }
            } else {
                log.debug("{} not the leader.", heartbeaterValue);
            }
//            if (log.isDebugEnabled()) {
//                log.debug("results: {}", result);
//            }
        };
        cancellableFuture = scheduledExecutorService.scheduleWithFixedDelay(task, initialDelaySec, delaySec, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws Exception {
        log.warn("{} Heartbeater shutting down...", heartbeaterValue);
        cancellableFuture.cancel(true);
        scheduledExecutorService.shutdown();
    }

    RedisConsumerHeartbeater setDelaySec(int delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    RedisConsumerHeartbeater setInitialDelaySec(int initialDelaySec) {
        this.initialDelaySec = initialDelaySec;
        return this;
    }

    RedisConsumerHeartbeater setHeartbeatExpirySec(int heartbeatExpirySec) {
        this.heartbeatExpirySec = heartbeatExpirySec;
        return this;
    }

    RedisConsumerHeartbeater setListExpiryFactor(int listExpiryFactor) {
        this.listExpiryFactor = listExpiryFactor;
        return this;
    }

    String getLeaderHeartbeaterKey() {
        return leaderHeartbeaterKey;
    }

    String getHeartbeaterValue() {
        return heartbeaterValue;
    }

    String getHeartbeaterListKey() {
        return heartbeaterListKey;
    }

    String getHeartbeaterHb() {
        return heartbeaterHb;
    }
}
