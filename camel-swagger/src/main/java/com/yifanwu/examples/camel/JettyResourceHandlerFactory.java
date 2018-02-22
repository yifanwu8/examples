package com.yifanwu.examples.camel;

import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * @author Yifan.Wu on 2/21/2018
 */
public class JettyResourceHandlerFactory {
    public static ResourceHandler buildResourceHandler(String resourcePath) throws Exception {
        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(JettyResourceHandlerFactory.class.getClassLoader()
                .getResource(resourcePath)
                .toURI().toString());
        return rh;
    }

}
