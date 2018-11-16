FROM ibmcom/mq:latest

LABEL maintainer="yifan.wu"

RUN mkdir /sh

COPY ./start-ibmmq.sh /sh/
RUN chown mqm:mqm /sh/start-ibmmq.sh

ENTRYPOINT []
CMD ["/sh/start-ibmmq.sh"]
