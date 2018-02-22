package com.yifanwu.examples.commons.docker;

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.Objects;

/**
 * @author Yifan.Wu on 10/17/2017
 */
public class DockerUtils {

    public static GenericContainer getContainer4Junit(DockerContainerProps props) {
        GenericContainer result;
        if (Objects.nonNull(props.getFixedExposedPorts()) && props.getFixedExposedPorts().size() > 0) {
            FixedHostPortGenericContainer container = new FixedHostPortGenericContainer(props.getImageName() + ":" + props.getImageVersion());
            for (Map.Entry<Integer, Integer> entry : props.getFixedExposedPorts().entrySet()) {
                container.withFixedExposedPort(entry.getKey(), entry.getValue());
            }
            result = container;
        } else {
            result = new GenericContainer(props.getImageName() + ":" + props.getImageVersion());
        }
        result.withExposedPorts(props.getExposedPorts().toArray(new Integer[props.getExposedPorts().size()]));
        result.withEnv(props.getEnvs());

        return result;
    }
}
