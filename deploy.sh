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
    git pull origin chore/github-action
elif [ "$PROFILE" == "prod" ]; then
    git pull origin main
fi

echo "Gradle Build"
chmod +x ./gradlew
./gradlew build

# ==================== 여기부터 교체 ====================

echo "--- Starting Debug Mode ---"
set -x # 실행되는 모든 명령어를 터미널에 + 기호와 함께 보여줍니다.

CURRENT_PID=$(pgrep -f "${JAR_NAME}.*\.jar.*spring\.profiles\.active=$PROFILE" || true)
echo "Found PID: [$CURRENT_PID]" # 변수 확인을 위해 대괄호로 감쌉니다.

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
echo "Found JAR file: [$JAR_FILE]" # 변수 확인을 위해 대괄호로 감쌉니다.

mkdir -p "$LOG_DIR"
LOG_PATH="$LOG_DIR/server_log_${PROFILE}_$(date +%Y-%m-%d).txt"

nohup java -jar -Dspring.profiles.active=$PROFILE "$JAR_FILE" >> "$LOG_PATH" 2>&1 &

set +x # 디버깅 모드 종료
echo "--- Exiting Debug Mode ---"

find build/libs -type f -name "*.jar" -mtime +3 -delete

echo "Deployment complete for $PROFILE"
