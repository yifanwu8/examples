package com.yifanwu.examples.commons.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * This implementation is tightly coupled with spotify/kafka docker image.
 * Beware, there is no dynamic port binding at the moment. so 1 docker host can only run one container from the same
 * properties at a time
 * @author Yifan.Wu on 10/17/2017
 */
public class KafkaDockerProps extends DockerContainerProps {

    public static final Logger log = LoggerFactory.getLogger(KafkaDockerProps.class);

    public static final String PROPS_FILE_DEFAULT = "docker/kafka-default.properties";
//    ports that are in the image (can not be changed)
    public static final Integer KAFKA_BROKER_PORT_DEFAULT = 9092;
    public static final Integer ZOOKEEPER_PORT_DEFAULT = 2181;

    public static final String ADVERTISED_HOST_ENV_KEY = "ADVERTISED_HOST";
    public static final String ADVERTISED_PORT_ENV_KEY = "ADVERTISED_PORT";

    public static final String  KAFKA_IMAGE_PROP_KEY = "docker.kafka.image";
    public static final String KAFKA_VERSION_PROP_KEY = "docker.kafka.version";
    public static final String KAFKA_HOST_PROP_KEY = "docker.kafka.advertised.host";
    public static final String KAFKA_ADVERTISED_PORT_PROP_KEY = "docker.kafka.advertised.port";

    public static GenericContainer getDefaultKafkaContainer() {
        return DockerUtils.getContainer4Junit(getKafkaPropsFromDefault());
    }

    public static KafkaDockerProps getKafkaPropsFromDefault() {
        return getKafkaPropsFromFile(PROPS_FILE_DEFAULT);
    }

    public static KafkaDockerProps getKafkaPropsFromFile(String uri) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = KafkaDockerProps.class.getClassLoader().getResourceAsStream(uri);
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
        String imageName = props.getProperty(KAFKA_IMAGE_PROP_KEY);
        String imageVersion = props.getProperty(KAFKA_VERSION_PROP_KEY, IMAGE_VERSION_DEFAULT);
        Objects.requireNonNull(imageName);

        KafkaDockerProps kafkaDockerProps = new KafkaDockerProps(imageName, imageVersion);
        kafkaDockerProps
            .addFixedExposedPort(Integer.parseInt(props.getProperty(KAFKA_ADVERTISED_PORT_PROP_KEY)), KAFKA_BROKER_PORT_DEFAULT)
            .addExposedPort(ZOOKEEPER_PORT_DEFAULT)
            .addEnv(ADVERTISED_HOST_ENV_KEY, props.getProperty(KAFKA_HOST_PROP_KEY))
                .addEnv(ADVERTISED_PORT_ENV_KEY, props.getProperty(KAFKA_ADVERTISED_PORT_PROP_KEY));

        return kafkaDockerProps;
    }

    public KafkaDockerProps(String imageName) {
        super(imageName);
    }

    public KafkaDockerProps(String imageName, String imageVersion) {
        super(imageName, imageVersion);
    }
}
