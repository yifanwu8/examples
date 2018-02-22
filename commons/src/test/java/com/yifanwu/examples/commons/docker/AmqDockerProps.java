package com.yifanwu.examples.commons.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Yifan.Wu on 10/17/2017
 */
public class AmqDockerProps extends DockerContainerProps {

    public static final Logger log = LoggerFactory.getLogger(AmqDockerProps.class);

    public static final String PROPS_FILE_DEFAULT = "docker/amq-default.properties";
//    ports that are in the image (can not be changed)
    public static final Integer JMS_PORT_DEFAULT = 61616;
    public static final Integer AMQ_UI_PORT_DEFAULT = 8161;

    public static final String ACTIVEMQ_CONFIG_MINMEMORY_ENV_KEY = "ACTIVEMQ_CONFIG_MINMEMORY";
    public static final String ACTIVEMQ_CONFIG_MAXMEMORY_ENV_KEY = "ACTIVEMQ_CONFIG_MAXMEMORY";

    public static final String AMQ_IMAGE_PROP_KEY = "docker.amq.image";
    public static final String AMQ_VERSION_PROP_KEY = "docker.amq.version";
    public static final String ACTIVEMQ_CONFIG_MINMEMORY_PROP_KEY = "docker.amq.memory.min";
    public static final String ACTIVEMQ_CONFIG_MAXMEMORY_PROP_KEY = "docker.amq.memory.max";

    public static GenericContainer getDefaultContainer() {
        return DockerUtils.getContainer4Junit(getPropsFromDefault());
    }

    public static AmqDockerProps getPropsFromDefault() {
        return getPropsFromFile(PROPS_FILE_DEFAULT);
    }

    public static AmqDockerProps getPropsFromFile(String uri) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = AmqDockerProps.class.getClassLoader().getResourceAsStream(uri);
            props.load(inputStream);
        } catch (Exception e) {
            log.warn("Failed to read the file={} as properties with excepiton {}", uri, e);
        } finally {
            if (Objects.nonNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Failed to close input stream exception {}", e);
                }
            }
        }
        String imageName = props.getProperty(AMQ_IMAGE_PROP_KEY);
        String imageVersion = props.getProperty(AMQ_VERSION_PROP_KEY, IMAGE_VERSION_DEFAULT);
        Objects.requireNonNull(imageName);

        AmqDockerProps dockerProps = new AmqDockerProps(imageName, imageVersion);
        dockerProps
            .addExposedPort(JMS_PORT_DEFAULT)
            .addExposedPort(AMQ_UI_PORT_DEFAULT)
            .addEnv(ACTIVEMQ_CONFIG_MINMEMORY_ENV_KEY, props.getProperty(ACTIVEMQ_CONFIG_MINMEMORY_PROP_KEY))
                .addEnv(ACTIVEMQ_CONFIG_MAXMEMORY_ENV_KEY, props.getProperty(ACTIVEMQ_CONFIG_MAXMEMORY_PROP_KEY));

        return dockerProps;
    }

    public AmqDockerProps(String imageName) {
        super(imageName);
    }

    public AmqDockerProps(String imageName, String imageVersion) {
        super(imageName, imageVersion);
    }
}
