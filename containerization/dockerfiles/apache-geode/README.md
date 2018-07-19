# Build ready-to-go Apache Geode service docker image

Dockerfiles that build ready-to-go Apache geode service. No need to use gfsh command.


#### [single server configuration](https://github.com/yifanwu8/examples/tree/master/containerization/dockerfiles/apache-geode/single_server)
This configuration start one locator and one server.
It is also capable of creating up to 4 regions at the container start up time by passing environment variables.
The region is only replicated because there is only one server.
The expiry time in second can be passed in by environment variable.
If the clients is not on localhost, a ADDRESS environment variable is also needed which is the ip address or
 hostname of the server reachable from client.
```
docker run -p 10334:10334 -p 40404:40404 -e REGION1=region1 -e REGION1_EXPIRE=100 -e REGION2=num2 -e REGION2_EXPIRE=300 -e REGION3=reg3 -e REGION3_EXPIRE=300 -e ADDRESS=aaa.bbb.ccc.ddd yifanwu/apachegeode:replicated4
```

##### Security
If you want to turn on security, please set `SECURITY` environment variable to `true` and provide `USERNAME`and `PASSWORD`. Please see below example.
The security manager is Geode embedded `ExampleSecurityManager` which will take a security.json file. The internal security.json file is shown in the [Appendix](#Appendix).
If you want to use your own security.json file, please do a volume map to `/classpath`.
```bash
docker run -p 10334:10334 -p 40404:40404 -e SECURITY=true -e USERNAME=super-user -e PASSWORD=1234567 -e REGION1=region1 -e REGION1_EXPIRE=100 yifanwu/apachegeode:replicated4
```

### Appendix
##### security.json
```json
{
  "roles": [
    {
      "name": "cluster",
      "operationsAllowed": [
        "CLUSTER:MANAGE",
        "CLUSTER:WRITE",
        "CLUSTER:READ"
      ]
    },
    {
      "name": "data",
      "operationsAllowed": [
        "DATA:MANAGE",
        "DATA:WRITE",
        "DATA:READ"
      ]
    },
    {
      "name": "region1&2Reader",
      "operationsAllowed": [
        "DATA:READ"
      ],
      "regions": ["region1", "region2"]
    }
  ],
  "users": [
    {
      "name": "super-user",
      "password": "1234567",
      "roles": [
        "cluster",
        "data"
      ]
    },
    {
      "name": "joebloggs",
      "password": "1234567",
      "roles": [
        "data"
      ]
    }
  ]
}
```