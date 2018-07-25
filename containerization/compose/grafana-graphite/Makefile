prep :
	mkdir -p \
		data/graphite/storage \
		data/grafana
	chmod 0777 data/grafana

pull :
	docker-compose pull

up : prep pull
	docker-compose up

upd : prep pull
	docker-compose up -d

down :
	docker-compose down
