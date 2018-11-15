package com.yifanwu.examples.mq;

/**
 * Message Queue Producer API for produce single message per call
 * @author Yifan.Wu on 7/16/2018
 */
public interface SiMessageProducer<T> {

    /**
     * send single message
     * @param message
     * @return true for success
     */
    boolean send(T message);

    SiQueue getDestination();
}
