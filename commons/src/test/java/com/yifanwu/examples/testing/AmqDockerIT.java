package com.yifanwu.examples.testing;

import com.yifanwu.examples.commons.docker.AmqDockerProps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * @author Yifan.Wu on 10/13/2017
 */
public class AmqDockerIT {

    public static final Logger log = LoggerFactory.getLogger(AmqDockerIT.class);

    @ClassRule
    public static GenericContainer amqContainer = AmqDockerProps.getDefaultContainer();

    @Before
    public void setUp() {
        System.out.println(amqContainer.getExposedPorts());
    }

    /**
     * do your testing with service hosted in Docker container.
     */
    @Test
    public void simpleIT() {
        Assert.assertTrue(amqContainer.isRunning());
        Assert.assertNotNull(amqContainer.getContainerId());
    }
}
