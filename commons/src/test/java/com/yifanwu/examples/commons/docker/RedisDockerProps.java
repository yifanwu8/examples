package com.yifanwu.examples.commons.docker;

import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Yifan.Wu on 3/7/2018
 */
public class RedisDockerProps extends DockerContainerProps {
    private static final Logger log = getLogger(RedisDockerProps.class);

    public static final String PROPS_FILE_DEFAULT = "docker/redis-default.properties";
    //    ports that are in the image (can not be changed)
    public static final Integer REDIS_PORT_DEFAULT = 6379;

    public static final String REDIS_IMAGE_PROP_KEY = "docker.redis.image";
    public static final String REDIS_VERSION_PROP_KEY = "docker.redis.version";

    public static GenericContainer getDefaultContainer() {
        return DockerUtils.getContainer4Junit(getPropsFromDefault());
    }

    public static RedisDockerProps getPropsFromDefault() {
        return getPropsFromFile(PROPS_FILE_DEFAULT);
    }

    public static RedisDockerProps getPropsFromFile(String uri) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = RedisDockerProps.class.getClassLoader().getResourceAsStream(uri);
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
        String imageName = props.getProperty(REDIS_IMAGE_PROP_KEY);
        String imageVersion = props.getProperty(REDIS_VERSION_PROP_KEY, DockerContainerProps.IMAGE_VERSION_DEFAULT);
        Objects.requireNonNull(imageName);

        RedisDockerProps dockerProps = new RedisDockerProps(imageName, imageVersion);
        dockerProps
                .addExposedPort(REDIS_PORT_DEFAULT);

        return dockerProps;
    }

    public RedisDockerProps(String imageName) {
        super(imageName);
    }

    public RedisDockerProps(String imageName, String imageVersion) {
        super(imageName, imageVersion);
    }
}
