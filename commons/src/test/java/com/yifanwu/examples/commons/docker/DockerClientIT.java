package com.yifanwu.examples.commons.docker;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy implementation. DO not use anymore.
 * @author Yifan.Wu on 10/9/2017
 */
@Ignore
public class DockerClientIT {

    private DockerEngineClient dockerClient;

    @Before
    public void setUp() throws DockerCertificateException {
        dockerClient = new DockerEngineClient("http://localhost:2375");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void simpleIT() throws DockerException, InterruptedException {
        dockerClient.getDocker().pull("spotify/kafka");

        // Bind container ports to host ports
        final String[] ports = {"9092", "2181"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        // Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("spotify/kafka")
                .exposedPorts(ports)
                .env("ADVERTISED_PORT=9092", "ADVERTISED_HOST=10.0.75.1")
                .build();

        final ContainerCreation creation = dockerClient.getDocker().createContainer(containerConfig);
        final String id = creation.id();

// Inspect container
        final ContainerInfo info = dockerClient.getDocker().inspectContainer(id);

// Start container
        dockerClient.getDocker().startContainer(id);

        // Kill container
        dockerClient.getDocker().killContainer(id);

// Remove container
        dockerClient.getDocker().removeContainer(id);

// Close the docker client
        dockerClient.getDocker().close();

    }
}
