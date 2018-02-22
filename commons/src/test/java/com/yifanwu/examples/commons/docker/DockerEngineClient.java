package com.yifanwu.examples.commons.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * @author Yifan.Wu on 10/9/2017
 */
public class DockerEngineClient {

    private final String connStr;
    private final DockerClient docker;

    public DockerEngineClient(String connStr) throws DockerCertificateException {
        this.connStr = connStr;
        docker = DefaultDockerClient.builder().uri(connStr).build();
    }

    public DockerClient getDocker() {
        return docker;
    }

    public void pullImage(String image) throws DockerException, InterruptedException {
        docker.pull(image);
    }
}
