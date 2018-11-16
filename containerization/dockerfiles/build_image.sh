#!/bin/sh

# please run the script with this directory on a linux machine

ACCOUNT=yifanwu
IMAGE_NAME=someImageName
IMAGE_TAG=someTag

docker build -t $ACCOUNT/$IMAGE_NAME:$IMAGE_TAG .

docker push $ACCOUNT/$IMAGE_NAME:$IMAGE_TAG
