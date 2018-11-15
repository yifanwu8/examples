# Redis Message Queue
A simple message queue implementation backed by Redis.
It has dependency on spring-data-redis and jedis library.
All operations are single message based.


### Message Producer
Producer is send-and-forget. It is thread-safe.
Under the hood, it is `PUSH` to Redis list.
```java
SiQueue dest = new RedisQueue("destinationQueueName");
SiMessageProducer msgProducer = new RedisSingleMsgProducer<>(redisTemplate, dest);
msgProducer.send(someObject);  // someObject needs to be serializable by spring redisTemplate

```

### Message Consumer
Consumer has 3 APIs to consume single message: blocking, blocking w/ timeout and no wait.
Consumer needs to either explicitly call acknowledge or call receive for
 next message to acknowledge the success processing of the previous message.
It is NOT threadsafe. Each thread should have its own consumer.
Under the hood, each consumer has its own processing queue (a Redis List).
The processing queue have only one message and it is unacknowledged.
Consumer will do Reids `RPOPLPUSH` from waiting queue to processing queue.
Acknowledgement will delete the processing queue.
Crashed consumer's unacknowledged message will be collected by other
active consumer and pushed back to the waiting queue.
Heartbeating operation is vital part to detect crashed consumer and
do the necessary cleaning. The detail can be found in the Redis Lua "script".
```java
SiMessageConsumer msgConsumer = new RedisSingleMsgConsumer<>(redisTemplate, dest, "consumerName");
msgConsumer.init();
msgConsumer.receiveNoWait();
```
