package com.yifanwu.examples.mq.redis;

import com.yifanwu.examples.mq.SiMessageConsumer;
import com.yifanwu.examples.mq.SiQueue;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Single message queueing and dequeueing implementation backed by Redis
 * No auto-acknowledge. Need explicitly call acknowledge or receive next message for acknowledgement
 * It is not thread safe.
 * @author Yifan.Wu on 7/13/2018
 */
@NotThreadSafe
public class RedisSingleMsgConsumer<T> implements SiMessageConsumer<T>, AutoCloseable {
    private static final Logger log = getLogger(RedisSingleMsgConsumer.class);

    private volatile boolean isShutDown = false;

    private final String id;
    private final String name;

    private final RedisTemplate<String, T> redisTemplate;
    private final RedisSerializer<String> stringRedisSerializer;
    private final RedisSerializer<T> valueSerialer;
    private final RedisConsumerHeartbeater heartbeater;

    private final SiQueue processingQueue;
    private SiQueue waitQueue;

    // below parameters are for heartbeat
    private int initialDelaySec = 20;
    private int delaySec = 30;
    private int heartbeatExpirySec = 120;

    private volatile Optional<T> unAcknowledged = Optional.empty();

    public RedisSingleMsgConsumer(RedisTemplate<String, T> redisTemplate, SiQueue waitQueue, String name) throws IOException {
        id = UUID.randomUUID().toString();
        this.redisTemplate = redisTemplate;
        this.valueSerialer = (RedisSerializer<T>) redisTemplate.getValueSerializer();
        this.stringRedisSerializer = redisTemplate.getStringSerializer();
        this.waitQueue = waitQueue;
        this.name = name;
        processingQueue = new RedisQueue(name + RedisQueue.KEY_JOINER + id);

        heartbeater = new RedisConsumerHeartbeater(new StringRedisTemplate(redisTemplate.getConnectionFactory()), waitQueue, processingQueue);
    }

    /**
     * Register and start heartbeat
     */
    public void init() {
        heartbeater.setDelaySec(delaySec).setInitialDelaySec(initialDelaySec).setHeartbeatExpirySec(heartbeatExpirySec);
        heartbeater.init();  // init will call register as 1st step
    }

    @Override
    public Optional<T> receive() {
        return receiveHelper(true, 0);
    }

    /**
     * Execute 2 commands in pipeline
     * 1. Acknowledge the previously received messages by deleting the processing Q (list)
     * 2. BRPOPLPUSH from wait Q to processing Q
     * @param timeout in Sec
     * @return
     */
    @Override
    public Optional<T> receive(int timeout) {
        return receiveHelper(true, timeout);
    }

    private Optional<T> receiveHelper(boolean blocking, int timeout) {
        checkShutdownThrow();
        List<Object> results = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.keyCommands().del(stringRedisSerializer.serialize(processingQueue.getQueueName()));
                if (blocking) {
                    connection.listCommands().bRPopLPush(timeout, stringRedisSerializer.serialize(waitQueue.getQueueName()),
                            stringRedisSerializer.serialize(processingQueue.getQueueName()));
                } else {
                    connection.listCommands().rPopLPush( stringRedisSerializer.serialize(waitQueue.getQueueName()),
                            stringRedisSerializer.serialize(processingQueue.getQueueName()));
                }
                // Spring Redis mandates to return null
                return null;
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("Received results: {}", results);
        }
        if (results.get(0) instanceof Long) {
            Long deleted = (Long) results.get(0);
            log.debug("Processing Q deleted={}", deleted);
            if (unAcknowledged.isPresent() && deleted != 1) {
                log.warn("Have local unAcknowledged message but not able to acknowledge it during receive call. {}", deleted);
            }
        } else {
            log.error("Expecting integer/long but get {}", results.get(0));
        }
        Optional<T> res = Optional.ofNullable((T) results.get(1)); // cast exception
        unAcknowledged = res;
        return res;
    }

    @Override
    public Optional<T> receiveNoWait() {
        return receiveHelper(false, 1);
    }

    /**
     * acknowledge received message
     * @return
     */
    @Override
    public boolean acknowledge() {
        boolean res = redisTemplate.delete(processingQueue.getQueueName());
        if (!res && unAcknowledged.isPresent()) {
            log.warn("Have local unAcknowledged message but not able to acknowledge it during acknowledge call. processingQueueName={}", processingQueue.getQueueName());
        }
        unAcknowledged = Optional.empty();
        return res;
    }

    @Override
    public void close() throws Exception {
        log.info("Redis Consumer {} shutting down...", processingQueue.getQueueName());
        isShutDown = true;
        heartbeater.close();
    }

    @Override
    public SiQueue getSource() {
        return waitQueue;
    }

    private void checkShutdownThrow() {
        if (isShutDown) {
            throw new IllegalStateException("Consumer has been shutdown. ");
        }
    }

    public RedisSingleMsgConsumer setInitialDelaySec(int initialDelaySec) {
        this.initialDelaySec = initialDelaySec;
        return this;
    }

    public RedisSingleMsgConsumer setDelaySec(int delaySec) {
        this.delaySec = delaySec;
        return this;
    }

    public RedisSingleMsgConsumer setHeartbeatExpirySec(int heartbeatExpirySec) {
        this.heartbeatExpirySec = heartbeatExpirySec;
        return this;
    }

    // ONLY for test
    RedisConsumerHeartbeater getHeartbeater() {
        return heartbeater;
    }
    // ONLY for test
    String getProcessingQueueName() {
        return processingQueue.getQueueName();
    }
    // ONLY for test
    Optional<T> pendingMessage() {
        return unAcknowledged;
    }
}
