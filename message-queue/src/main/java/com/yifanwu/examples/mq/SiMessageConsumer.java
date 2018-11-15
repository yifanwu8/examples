package com.yifanwu.examples.mq;

import java.util.Optional;

/**
 * Message Queue Consumer API for consume single message per call
 * @author Yifan.Wu on 7/13/2018
 */
public interface SiMessageConsumer<T> {

    /**
     * Blocking to receive a message from the MQ
     * @return
     */
    Optional<T> receive();

    /**
     * Blocking with a time out to receive a message from the MQ
     * @param timeout time unit will depends on implementation
     * @return
     */
    Optional<T> receive(int timeout);

    /**
     * non-blocking receive a message
     * @return
     */
    Optional<T> receiveNoWait();

    /**
     * acknowledge received message(s)
     * @return
     */
    boolean acknowledge();

    SiQueue getSource();

}
