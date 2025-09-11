#!/bin/bash

NGINX_CONFIG_PATH=$(readlink /etc/nginx/conf.d/service-env.conf)
JAR_PATH="/home/ubuntu/sejong-life-prod/build/libs/server.jar"
CONFIG_PATH="file:/home/ubuntu/sejong-life-prod/"

if [[ -z "$NGINX_CONFIG_PATH" ]]; then
  echo "Nginx Config Not Found, Set Profile blue"
  CURRENT_PROFILE="none";
  PROFILE="blue"
elif [[ "$NGINX_CONFIG_PATH" == *"blue"* ]]; then
  CURRENT_PROFILE="blue"
  PROFILE="green"
else
  CURRENT_PROFILE="green"
  PROFILE="blue"
fi


echo "> Current Profile: $CURRENT_PROFILE"
echo "> Next Profile: $PROFILE"
echo ""

echo "Starting new server..."

nohup java -jar \
  -Dspring.profiles.active=${PROFILE} \
  -Dspring.config.location=${CONFIG_PATH} \
  ${JAR_PATH} > /dev/null 2>&1 &

echo "Health Check"

if [ "$PROFILE" == "blue" ]; then
    HEALTH_CHECK_PORT=8081
else
    HEALTH_CHECK_PORT=8082
fi
echo "> Port: $HEALTH_CHECK_PORT"
echo "> Wait For Start Server..."

RETRY_COUNT=1
MAX_RETRY_COUNT=24

while [ $RETRY_COUNT -le $MAX_RETRY_COUNT ]
do
    echo "> Try Health Check ($RETRY_COUNT / $MAX_RETRY_COUNT)"

    if curl -s http://127.0.0.1:${HEALTH_CHECK_PORT}/actuator/health | grep -q "UP"; then
        echo "> Health Check Success"
        break
    fi

    if [ $RETRY_COUNT -eq $MAX_RETRY_COUNT ]; then
        echo "> Health Check Failed, Stop Deploy."
        TARGET_PID=$(pgrep -f "spring.profiles.active=${PROFILE}")
        if [ ! -z "$TARGET_PID" ]; then
            kill -15 "$TARGET_PID"
        fi
        exit 1
    fi

    sleep 5
    RETRY_COUNT=$((RETRY_COUNT+1))
done
echo ""

echo "Nginx Traffic Change"
sudo ln -sf /etc/nginx/conf.d/service-${PROFILE}.conf /etc/nginx/conf.d/service-env.conf
sudo nginx -s reload
echo "Traffic Change Complete"
echo ""

if [ "$CURRENT_PROFILE" != "none" ]; then
    echo "TERMINATE OLD SERVER($CURRENT_PROFILE)"
    OLD_PID=$(pgrep -f "spring.profiles.active=${CURRENT_PROFILE}")
    if [ ! -z "$OLD_PID" ]; then
        kill -15 "$OLD_PID"
        sleep 5
    fi
fi

echo "Deploy Complete"

exit 0
