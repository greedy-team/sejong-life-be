#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "ERROR: please input profile parameter (dev or prod)"
    exit 1
fi

PROFILE=$1
PROJECT_DIR="/home/ubuntu/sejong-life-$PROFILE"
JAR_NAME="sejong-life-be"
LOG_DIR="$PROJECT_DIR/logs"

if [ "$PROFILE" == "dev" ]; then
    PORT=8081
    BRANCH="develop"
elif [ "$PROFILE" == "prod" ]; then
    PORT=8080
    BRANCH="main"
else
    echo "ERROR: Invalid profile. Please use 'dev' or 'prod'."
    exit 1
fi

cd $PROJECT_DIR

echo "========================================="
echo "Start deployment for PROFILE: $PROFILE"
echo "========================================="

echo ">> Pull repository from origin/$BRANCH"
git fetch origin $BRANCH
git reset --hard origin/$BRANCH

echo ">> Gradle Build"
chmod +x ./gradlew
./gradlew build

echo ">> Check running application on port $PORT"
CURRENT_PID=$(lsof -t -i:$PORT || true)

if [ -z "$CURRENT_PID" ]; then
    echo "No running application found."
else
    echo "Terminate running application: $CURRENT_PID"
    sudo kill -15 $CURRENT_PID
    sleep 5

    if ps -p $CURRENT_PID > /dev/null; then
        echo "Application did not terminate gracefully. Forcing kill."
        sudo kill -9 $CURRENT_PID
        sleep 2
    fi
    echo "Application terminated."
fi

echo ">> Deploy new application"
jars=(build/libs/*[!plain].jar)
JAR_FILE=${jars[0]}

mkdir -p "$LOG_DIR"
LOG_PATH="$LOG_DIR/server_log_${PROFILE}_$(date +%Y-%m-%d).txt"

nohup java -jar -Dspring.profiles.active=$PROFILE "$JAR_FILE" >> "$LOG_PATH" 2>&1 &

sleep 60
NEW_PID=$(lsof -t -i:$PORT || true)
if [ -n "$NEW_PID" ]; then
    echo ">> New application started successfully with PID: $NEW_PID"
else
    echo ">> ERROR: New application failed to start. Please check the log at $LOG_PATH"
    exit 1
fi

echo ">> Clean up old jar files"
find build/libs -type f -name "*.jar" -mtime +3 -delete

echo "========================================="
echo "Deployment complete for PROFILE: $PROFILE"
echo "========================================="
