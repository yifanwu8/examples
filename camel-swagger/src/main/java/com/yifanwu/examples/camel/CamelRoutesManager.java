package com.yifanwu.examples.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Yifan.Wu on 2/16/2018
 */
public class CamelRoutesManager {
    private static final Logger log = getLogger(CamelRoutesManager.class);

    private final CamelContext context;
    public static final String ID = "id";

    public CamelRoutesManager(CamelContext context) {
        this.context = context;
    }

    public List getRoutes() {
        return context.getRoutes().stream()
                .map(route -> {
                    Map<String, String> routeStr = new HashMap<>(2);
                    routeStr.put("id", route.getId());
                    routeStr.put("status", context.getRouteStatus(route.getId()).name());
                    return routeStr;
                })
                .collect(Collectors.toList());
    }

    @Deprecated
    public String stopRoute(Exchange exchange) throws Exception {
        String id = getHeader(exchange, ID);
        context.stopRoute(id);
        return context.getRouteStatus(id).name();
    }

    @Deprecated
    public String startRoute(Exchange exchange) throws Exception {
        String id = getHeader(exchange, ID);
        context.startRoute(id);
        return context.getRouteStatus(id).name();
    }

    public static String getHeader(Exchange exchange, String headerName) {
        return exchange.getIn().getHeader(headerName, String.class);
    }
}
