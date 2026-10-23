#!/bin/bash

COMPOSE_FILE="$HOME/docker-compose-dev.yml"

echo "> 새 앱 이미지 pull"
docker compose -f "${COMPOSE_FILE}" pull app

echo "> 컨테이너 실행 (mysql/redis는 유지, app만 교체)"
docker compose -f "${COMPOSE_FILE}" up -d

echo "> 헬스체크 시작"
RETRY_COUNT=1
MAX_RETRY_COUNT=24

while [ $RETRY_COUNT -le $MAX_RETRY_COUNT ]
do
    echo "> 헬스체크 진행중 ($RETRY_COUNT / $MAX_RETRY_COUNT)"

    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:9091/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "> 헬스체크 성공"
        break
    fi

    if [ $RETRY_COUNT -eq $MAX_RETRY_COUNT ]; then
        echo "> 헬스체크 실패, 앱 로그 출력 후 배포 중단"
        docker logs --tail 100 sejong-life-app
        exit 1
    fi

    sleep 5
    RETRY_COUNT=$((RETRY_COUNT+1))
done

echo "> 미사용 도커 이미지 삭제"
docker image prune -f

echo "> 배포 완료"
exit 0
