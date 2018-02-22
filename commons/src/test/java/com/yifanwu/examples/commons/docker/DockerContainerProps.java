package com.yifanwu.examples.commons.docker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Yifan.Wu on 10/16/2017
 */
public class DockerContainerProps {

    public static final String IMAGE_VERSION_DEFAULT = "latest";

    protected final String imageName;
    protected String imageVersion = IMAGE_VERSION_DEFAULT;

    protected Map<String, String> envs = new HashMap<>();
    protected Set<Integer> exposedPorts = new HashSet<>();
    protected Map<Integer, Integer> fixedExposedPorts = new HashMap<>();

    public DockerContainerProps(String imageName) {
        this.imageName = imageName;
    }

    public DockerContainerProps(String imageName, String imageVersion) {
        this.imageName = imageName;
        this.imageVersion = imageVersion;
    }

    public DockerContainerProps addEnv(String key, String value) {
        envs.put(key, value);
        return this;
    }

    public DockerContainerProps addExposedPort(int exposedPort) {
        exposedPorts.add(exposedPort);
        return this;
    }

    public DockerContainerProps addFixedExposedPort(int hostPort, int containerPort) {
        fixedExposedPorts.put(hostPort, containerPort);
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public DockerContainerProps setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
        return this;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public DockerContainerProps setEnvs(Map<String, String> envs) {
        this.envs = envs;
        return this;
    }

    public Set<Integer> getExposedPorts() {
        return exposedPorts;
    }

    public DockerContainerProps setExposedPorts(Set<Integer> exposedPorts) {
        this.exposedPorts = exposedPorts;
        return this;
    }

    public Map<Integer, Integer> getFixedExposedPorts() {
        return fixedExposedPorts;
    }

    public DockerContainerProps setFixedExposedPorts(Map<Integer, Integer> fixedExposedPorts) {
        this.fixedExposedPorts = fixedExposedPorts;
        return this;
    }
}
