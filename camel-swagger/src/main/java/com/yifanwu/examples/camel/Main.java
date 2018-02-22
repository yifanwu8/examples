package com.yifanwu.examples.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Yifan.Wu on 2/22/2018
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        String camelContextPath;
        if (Objects.nonNull(args) && args.length > 0) {
            camelContextPath = args[0].trim();
        } else {
            throw new IllegalArgumentException("Camel Context must be specified.");
        }

        LOG.debug("Loading camel-context from: " + camelContextPath);
        org.apache.camel.spring.Main main = new org.apache.camel.spring.Main();
        main.setFileApplicationContextUri(camelContextPath);
        main.start();
    }
}
