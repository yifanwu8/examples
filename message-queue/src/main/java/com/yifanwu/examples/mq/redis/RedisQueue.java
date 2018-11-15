package com.yifanwu.examples.mq.redis;


import com.yifanwu.examples.mq.SiQueue;

/**
 * @author Yifan.Wu on 7/16/2018
 */
public class RedisQueue implements SiQueue {
    public static final String KEY_JOINER = ":";

    private final String queueName;

    public RedisQueue(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }
}
