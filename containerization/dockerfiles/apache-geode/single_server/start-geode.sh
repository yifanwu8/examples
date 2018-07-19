#!/bin/sh

if [ -z "$ADDRESS" ]; then
    ADDRESS=localhost
fi
echo "Server address for clients: $ADDRESS"

if [ $SECURITY = "true" ]; then
    echo "Security is on."
    gfsh start locator --name=locator --port=10334 --hostname-for-clients=$ADDRESS --J=-Dgemfire.security-manager=org.apache.geode.examples.security.ExampleSecurityManager --classpath=/classpath
    gfsh start server --name=server1 --hostname-for-clients=$ADDRESS --server-port=40404 --locators=localhost[10334] --classpath=/classpath --user=$USERNAME --password=$PASSWORD
else
    gfsh start locator --name=locator --port=10334 --hostname-for-clients=$ADDRESS
    gfsh start server --name=server1 --hostname-for-clients=$ADDRESS --server-port=40404 --locators=localhost[10334]
fi

if [ ! -z "$REGION1" ]; then
    echo "Creating Region: $REGION1"
    if [ -z "$REGION1_EXPIRE" ]; then
        REGION1_EXPIRE=0
    fi
    if [ $SECURITY = "true" ]; then
        gfsh -e "connect --locator=localhost[10334] --user=$USERNAME --password=$PASSWORD" -e "create region --name=$REGION1 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION1_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    else
        gfsh -e "connect --locator=localhost[10334]" -e "create region --name=$REGION1 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION1_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    fi
fi

if [ ! -z "$REGION2" ]; then
    echo "Creating Region: $REGION2"
    if [ -z "$REGION2_EXPIRE" ]; then
        REGION2_EXPIRE=0
    fi
    if [ $SECURITY = "true" ]; then
        gfsh -e "connect --locator=localhost[10334] --user=$USERNAME --password=$PASSWORD" -e "create region --name=$REGION2 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION2_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    else
        gfsh -e "connect --locator=localhost[10334]" -e "create region --name=$REGION2 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION2_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    fi
fi

if [ ! -z "$REGION3" ]; then
    echo "Creating Region: $REGION3"
    if [ -z "$REGION3_EXPIRE" ]; then
        REGION3_EXPIRE=0
    fi
    if [ $SECURITY = "true" ]; then
        gfsh -e "connect --locator=localhost[10334] --user=$USERNAME --password=$PASSWORD" -e "create region --name=$REGION3 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION3_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    else
        gfsh -e "connect --locator=localhost[10334]" -e "create region --name=$REGION3 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION3_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    fi
fi

if [ ! -z "$REGION4" ]; then
    echo "Creating Region: $REGION4"
    if [ -z "$REGION4_EXPIRE" ]; then
        REGION4_EXPIRE=0
    fi
    if [ $SECURITY = "true" ]; then
        gfsh -e "connect --locator=localhost[10334] --user=$USERNAME --password=$PASSWORD" -e "create region --name=$REGION4 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION4_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    else
        gfsh -e "connect --locator=localhost[10334]" -e "create region --name=$REGION4 --type=REPLICATE --enable-statistics --entry-time-to-live-expiration=$REGION4_EXPIRE --entry-time-to-live-expiration-action=DESTROY"
    fi
fi

#spin forever
while true; do
    sleep 10;
done
