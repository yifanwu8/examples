#!/bin/sh

if [ ! -z "$QUEUES" ]; then
    QS=$(echo $QUEUES | tr "," "\n")

    for ONEQ in $QS
    do
        echo "set to create queue: $ONEQ"
        sed -i "/DEFINE QLOCAL('DEV.QUEUE.3') REPLACE/a DEFINE QLOCAL('${ONEQ}') REPLACE" /etc/mqm/10-dev.mqsc.tpl
    done
fi

runmqdevserver
