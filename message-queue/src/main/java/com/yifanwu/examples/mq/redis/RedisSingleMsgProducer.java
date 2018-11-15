package com.yifanwu.examples.mq.redis;

import com.yifanwu.examples.mq.SiMessageProducer;
import com.yifanwu.examples.mq.SiQueue;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Yifan.Wu on 7/16/2018
 */
@ThreadSafe
public class RedisSingleMsgProducer<T> implements SiMessageProducer<T> {
    private static final Logger log = getLogger(RedisSingleMsgProducer.class);

    private final RedisTemplate<String, T> redisTemplate;
    private final SiQueue destination;

    public RedisSingleMsgProducer(RedisTemplate<String, T> redisTemplate, SiQueue destination) {
        this.redisTemplate = redisTemplate;
        this.destination = destination;
    }

    @Override
    public boolean send(T message) {
        Long res = redisTemplate.opsForList().leftPush(destination.getQueueName(), message);
        log.debug("Destination Q={} size {}", destination.getQueueName(), res);
        return res > 0;
    }

    @Override
    public SiQueue getDestination() {
        return destination;
    }
}
