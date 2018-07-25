# Grafana with Graphite

#### Ports
The following ports must be available on the host:
- 3000 : Grafana Http port
- 2003 : Graphite/Carbon receiving port
- 8125 : Statsd port in Graphite

##### Start up
To start up the environment (docker compose) in the foreground (to see all logs):
```bash
make up
```
To start up the environment (docker compose) in the background:
```bash
make upd
```
The start up process will take few minutes depends on your resources.

##### Shut down
```bash
make down
```

##### Grafana
Point your favorite browser to `http://hostname:3000/`.
Login use: `admin/admin`

##### Persistence on host
Docker Volume is used to map container filesystem to host filesystem.  
`data` includes grafana dashboards and time-series data of Graphite.
`conf` includes Grafana Provisioning and dashboards.
