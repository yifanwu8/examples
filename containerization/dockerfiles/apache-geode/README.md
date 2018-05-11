# Build ready-to-go Apache Geode service docker image

Dockerfiles that build ready-to-go Apache geode service. No need to use gfsh command.


#### single server configuration
This configuration start one locator and one server.
It is also capable of creating up to 4 regions at the container start up time by passing environment variables.
The region is only replicated because there is only one server.
The expiry time in second can be passed in by environment variable.
If the clients is not on localhost, a ADDRESS environment variable is also needed which is the ip address or
 hostname of the server reachable from client.
```
 docker run -p 10334:10334 -p 40404:40404 --env REGION1=region1 --env REGION1_EXPIRE=100 --env REGION2=num2 --env REGION2_EXPIRE=300 --env REGION3=reg3 --env REGION3_EXPIRE=300 --env ADDRESS=aaa.bbb.ccc.ddd yifanwu/geode:latest
```