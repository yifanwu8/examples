package com.yifanwu.examples.mq.redis;

import com.yifanwu.examples.commons.EqualableRecords;
import com.yifanwu.examples.commons.docker.RedisDockerProps;
import com.yifanwu.examples.mq.SiMessageProducer;
import com.yifanwu.examples.mq.SiQueue;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Yifan.Wu on 7/16/2018
 */
public class RedisMsgQueueIT {
    private static final Logger log = getLogger(RedisMsgQueueIT.class);

    public static final String CONSUMER_NAME_1 = "testRedConsumer";
    public static final String DEST_QUEUE_NAME_1 = "aQname";

    public static final String MULT_DEST_QUEUE_NAME = "multQueue";
    public static final String MULT_CONSUMER_NAME_1 = "multCon1";
    public static final String MULT_CONSUMER_NAME_2 = "multCon2";
    public static final String MULT_CONSUMER_NAME_3 = "multCon3";

    @ClassRule
    public static GenericContainer redisContainer = RedisDockerProps.getDefaultContainer();

    public static void flush(RedisTemplate redisTemplate) {
        try {
            redisTemplate.execute(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    connection.flushDb();
                    return "FLUSHED";
                }
            });
        } catch (Exception e) {
            log.warn("Exception during Redis FlushDb command. e={}", e);
        }
    }

    protected static RedisTemplate<String, String> redisTemplate;

    private RedisSingleMsgProducer<String> msgProducer;
    private RedisSingleMsgConsumer<String> msgConsumer;

    @BeforeClass
    public static void setUpClass() {
        //below section is how to set up redis cluster:
//        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration()
//                .clusterNode("localhost", 7000)
//                .clusterNode("localhost", 7001)
//                .clusterNode("localhost", 7002);
//        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration);
        // end of how to set up redis cluster
        int port = redisContainer.getMappedPort(RedisDockerProps.REDIS_PORT_DEFAULT);
        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration("localhost", port);
        JedisClientConfiguration jedisClientConfiguration = new JedisClientConfiguration() {
            @Override
            public boolean isUseSsl() {
                return false;
            }

            @Override
            public Optional<SSLSocketFactory> getSslSocketFactory() {
                return Optional.empty();
            }

            @Override
            public Optional<SSLParameters> getSslParameters() {
                return Optional.empty();
            }

            @Override
            public Optional<HostnameVerifier> getHostnameVerifier() {
                return Optional.empty();
            }

            @Override
            public boolean isUsePooling() {
                return true;
            }

            @Override
            public Optional<GenericObjectPoolConfig> getPoolConfig() {
                return Optional.of(new JedisPoolConfig());
            }

            @Override
            public Optional<String> getClientName() {
                return Optional.empty();
            }

            @Override
            public Duration getConnectTimeout() {
                return Duration.ofSeconds(30);
            }

            @Override
            public Duration getReadTimeout() {
                return Duration.ofSeconds(30);
            }
        };

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
        jedisConnectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
    }

    @Before
    public void setUp() {
        flush(redisTemplate);
    }

    @After
    public void tearDown() {
        msgConsumer = null;
        msgProducer = null;
    }

    private void prepare() throws IOException {
        prepare(120, 5, 30);
    }
    private void prepare(int heartbeatExpirySec, int initialDelaySec, int delaySec) throws IOException {
        prepare(heartbeatExpirySec, 30, initialDelaySec, delaySec);
    }
    private void prepare(int heartbeatExpirySec, int listExpiryFactor, int initialDelaySec, int delaySec) throws IOException {
        SiQueue dest = new RedisQueue(DEST_QUEUE_NAME_1);

        msgProducer = new RedisSingleMsgProducer<>(redisTemplate, dest);
        msgConsumer = new RedisSingleMsgConsumer<>(redisTemplate, dest, CONSUMER_NAME_1);
        prepareConsumer(msgConsumer, heartbeatExpirySec, listExpiryFactor, initialDelaySec, delaySec);
    }

    private void prepareConsumer(RedisSingleMsgConsumer consumer, int heartbeatExpirySec, int listExpiryFactor, int initialDelaySec, int delaySec) {
        consumer.setHeartbeatExpirySec(heartbeatExpirySec).setInitialDelaySec(initialDelaySec).setDelaySec(delaySec);
        consumer.getHeartbeater().setListExpiryFactor(listExpiryFactor);
        consumer.init();
    }

    @Test
    public void SimpleSendReceiveIT() throws IOException {
        prepare();

        String aMsg = "A msg";
        String msg1 = "1 msg";
        String msg2 = "2 msg";

        Assert.assertFalse(msgConsumer.receiveNoWait().isPresent());
        msgProducer.send(aMsg);
        Assert.assertFalse(redisTemplate.hasKey(msgConsumer.getProcessingQueueName()));
        Assert.assertFalse(msgConsumer.pendingMessage().isPresent());
        Assert.assertEquals(aMsg, msgConsumer.receive().get());
        Assert.assertTrue(redisTemplate.hasKey(msgConsumer.getProcessingQueueName()));
        Assert.assertTrue(msgConsumer.pendingMessage().isPresent());
        Assert.assertFalse(msgConsumer.receiveNoWait().isPresent());
        // acknowledged by call receive
        Assert.assertFalse(redisTemplate.hasKey(msgConsumer.getProcessingQueueName()));
        Assert.assertFalse(msgConsumer.pendingMessage().isPresent());

        msgProducer.send(msg1);
        msgProducer.send(msg2);
        Assert.assertEquals(msg1, msgConsumer.receive().get());
        Assert.assertTrue(redisTemplate.hasKey(msgConsumer.getProcessingQueueName()));
        Assert.assertTrue(msgConsumer.pendingMessage().isPresent());

        msgConsumer.acknowledge();
        Assert.assertFalse(redisTemplate.hasKey(msgConsumer.getProcessingQueueName()));
        Assert.assertFalse(msgConsumer.pendingMessage().isPresent());

        Assert.assertEquals(msg2, msgConsumer.receive().get());
        Assert.assertFalse(msgConsumer.receiveNoWait().isPresent());

    }

    @Test
    public void registerAndExpireIT() throws InterruptedException, IOException {
        SiQueue dest = new RedisQueue(DEST_QUEUE_NAME_1);
        msgConsumer = new RedisSingleMsgConsumer<>(redisTemplate, dest, CONSUMER_NAME_1);
        // NOT inited
        Assert.assertFalse(isInList(msgConsumer));
        Assert.assertNull(getHeartbeat(msgConsumer));

        prepare(1, 1, 30, 60);
        // registered and inited
        Assert.assertTrue(isInList(msgConsumer));
        Assert.assertEquals(msgConsumer.getHeartbeater().getHeartbeaterHb(), getHeartbeat(msgConsumer));
        // heartbeat should expired after 1 sec
        Thread.sleep(1100L);
        Assert.assertNull(getHeartbeat(msgConsumer));
        Assert.assertFalse(isInList(msgConsumer));
    }

    @Test
    public void scriptSimpIT() throws Exception {
        SiQueue dest = new RedisQueue(DEST_QUEUE_NAME_1);
        msgConsumer = new RedisSingleMsgConsumer<>(redisTemplate, dest, CONSUMER_NAME_1);
        prepare(2, 2, 1, 1);
        // registered and inited
        Assert.assertTrue(isInList(msgConsumer));
        Assert.assertEquals(msgConsumer.getHeartbeater().getHeartbeaterHb(), getHeartbeat(msgConsumer));
        // oversleep with heartbeating in the background
        Thread.sleep(2100L);
        Assert.assertEquals(msgConsumer.getHeartbeater().getHeartbeaterHb(), getHeartbeat(msgConsumer));
        Assert.assertTrue(isInList(msgConsumer));

        msgConsumer.close();
        Thread.sleep(2100L);
        Assert.assertNull(getHeartbeat(msgConsumer));
        Assert.assertTrue(isInList(msgConsumer));
        Thread.sleep(2100L);
        Assert.assertFalse(isInList(msgConsumer));

    }

    @Test
    public void multiConsumerSimpIT() throws Exception {
        SiQueue dest = new RedisQueue(MULT_DEST_QUEUE_NAME);
        SiMessageProducer<String> producer = new RedisSingleMsgProducer<>(redisTemplate, dest);

        String msg1 = "msg1";
        String msg2 = "msg2";
        String msg3 = "msg3";
        EqualableRecords<String> sendingRepo = new EqualableRecords<>();
        EqualableRecords<String> receivingRepo = new EqualableRecords<>();

        producer.send(msg1);
        producer.send(msg2);
        producer.send(msg3);
        sendingRepo.add(msg3);

        RedisSingleMsgConsumer<String> consumer1 = new RedisSingleMsgConsumer<>(redisTemplate, dest, MULT_CONSUMER_NAME_1);
        prepareConsumer(consumer1, 2, 2, 1, 1);
        RedisSingleMsgConsumer<String> consumer2 = new RedisSingleMsgConsumer<>(redisTemplate, dest, MULT_CONSUMER_NAME_2);
        prepareConsumer(consumer2, 2, 2, 1, 1);
        RedisSingleMsgConsumer<String> consumer3 = new RedisSingleMsgConsumer<>(redisTemplate, dest, MULT_CONSUMER_NAME_3);
        prepareConsumer(consumer3, 2, 2, 1, 1);

        Assert.assertFalse(redisTemplate.hasKey(consumer1.getProcessingQueueName()));
        consumer1.receive(1);
        Assert.assertTrue(redisTemplate.hasKey(consumer1.getProcessingQueueName()));

        Assert.assertFalse(redisTemplate.hasKey(consumer2.getProcessingQueueName()));
        consumer2.receive(1);
        Assert.assertTrue(redisTemplate.hasKey(consumer2.getProcessingQueueName()));

        receivingRepo.add(consumer3.receive(1).get());
        Assert.assertTrue(redisTemplate.hasKey(consumer3.getProcessingQueueName()));
        Assert.assertEquals(sendingRepo, receivingRepo); // only consumer3 should receive the last message

        Thread.sleep(1100L);

        consumer3.receive(1).ifPresent(o -> receivingRepo.add(o));
        consumer3.receive(1).ifPresent(o -> receivingRepo.add(o));
        Assert.assertEquals(sendingRepo, receivingRepo);

        // shut down without acknowledge
        consumer1.close();
        consumer2.close();

        // oversleep and orphaned messages should be requeued back to wait Q.
        Thread.sleep(4100L);

        consumer3.receive(1).ifPresent(o -> receivingRepo.add(o));
        consumer3.receive(1).ifPresent(o -> receivingRepo.add(o));

        sendingRepo.add(msg1);
        sendingRepo.add(msg2);
        Assert.assertEquals(sendingRepo, receivingRepo);

        Assert.assertFalse(redisTemplate.hasKey(consumer1.getProcessingQueueName()));
        Assert.assertFalse(redisTemplate.hasKey(consumer2.getProcessingQueueName()));
        Assert.assertTrue(redisTemplate.hasKey(consumer3.getProcessingQueueName()));

        consumer3.acknowledge();
        Assert.assertFalse(redisTemplate.hasKey(consumer3.getProcessingQueueName()));
    }

    private boolean isInList(RedisSingleMsgConsumer<String> msgConsumer) {
        List<String> allConsumers = redisTemplate.opsForList().range(msgConsumer.getHeartbeater().getHeartbeaterListKey(), 0, -1);
        return allConsumers.contains(msgConsumer.getProcessingQueueName());
    }

    private String getHeartbeat(RedisSingleMsgConsumer<String> msgConsumer) {
        return redisTemplate.opsForValue().get(msgConsumer.getHeartbeater().getHeartbeaterHb());

    }

}
