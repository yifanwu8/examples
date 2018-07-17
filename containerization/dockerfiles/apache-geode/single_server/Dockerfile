FROM apachegeode/geode:latest

LABEL maintainer="yifan.wu"

RUN mkdir /sh

COPY ./start-geode.sh /sh/

RUN mkdir /classpath
COPY ./security.json /classpath/

CMD ["/sh/start-geode.sh"]
