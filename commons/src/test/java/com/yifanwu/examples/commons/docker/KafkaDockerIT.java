package com.yifanwu.examples.commons.docker;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

/**
 * Ignored. only for testing for testing code
 * @author Yifan.Wu on 10/13/2017
 */
@Ignore
public class KafkaDockerIT {

    public static final Logger log = LoggerFactory.getLogger(KafkaDockerIT.class);

    private static GenericContainer tempCont = new FixedHostPortGenericContainer("spotify/kafka:latest")
            .withFixedExposedPort(9092, 9092).withFixedExposedPort(2181, 2181)
            .withEnv("ADVERTISED_HOST", "10.0.75.1").withEnv("ADVERTISED_PORT", "9092");

    @ClassRule
    public static GenericContainer kafkaContainer = tempCont;

    @Before
    public void setUp() {
        System.out.println(kafkaContainer.getExposedPorts());
    }

    @Test
    public void simpleIT() {
        System.out.println(kafkaContainer.getExposedPorts());
    }
}
