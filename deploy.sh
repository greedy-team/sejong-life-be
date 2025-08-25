#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "ERROR: please input profile parameter"
    exit 1
fi

PROFILE=$1
PROJECT_DIR="/home/ubuntu/sejong-life-$PROFILE"
JAR_NAME="sejong-life"
LOG_DIR="$PROJECT_DIR/logs"

cd $PROJECT_DIR

echo "Start deployment for $PROFILE"
echo "Pull repository"

if [ "$PROFILE" == "dev" ]; then
    git fetch origin develop
    git reset --hard origin/develop
elif [ "$PROFILE" == "prod" ]; then
    git fetch origin main
    git reset --hard origin/main
fi

echo "Gradle Build"
chmod +x ./gradlew
./gradlew build

CURRENT_PID=$(pgrep -f "${JAR_NAME}.*\.jar.*spring\.profiles\.active=$PROFILE" || true)

if [ -z "$CURRENT_PID" ]; then
    echo "No running application for $PROFILE"
else
    echo "Terminate running application for $PROFILE: $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "Deploy new application for $PROFILE"

jars=(build/libs/*[!plain].jar)
JAR_FILE=${jars[0]}

mkdir -p "$LOG_DIR"
LOG_PATH="$LOG_DIR/server_log_${PROFILE}_$(date +%Y-%m-%d).txt"

nohup java -jar -Dspring.profiles.active=$PROFILE "$JAR_FILE" >> "$LOG_PATH" 2>&1 &

find build/libs -type f -name "*.jar" -mtime +3 -delete

echo "Deployment complete for $PROFILE"
