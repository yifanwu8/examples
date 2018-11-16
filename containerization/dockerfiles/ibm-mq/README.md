# IBM Message Queue with extra queues as environment variables

Dockerfiles that build IBM Message Queue with extra queues.
 The queues' names are passed in during container start up time as environment variables.

```
docker run -e LICENSE=accept -e MQ_QMGR_NAME=QM1 -e QUEUES=Q1,Q2,q3,q4 -p 1414:1414 -p 9443:9443 -d yifanwu/ibmmq
```

### Additional Configuration
The default configuration uses:
- Queue Manager: QM1
- Channel: DEV.ADMIN.SVRCONN
- Credentials: admin/passw0rd

Please see [IBM MQ IMAGE DOC](https://github.com/ibm-messaging/mq-container/tree/master/docs) for more information.

### Web console
https://hostname:9443/ibmmq/console/  
Credentials: admin/passw0rd

