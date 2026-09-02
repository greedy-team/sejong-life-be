#!/bin/bash

COMPOSE_FILE="$HOME/docker-compose-prod.yml"

IS_BLUE_EXIST=$(docker ps | grep blue)
IS_REDIS_EXIST=$(docker ps | grep redis)

if [ -n "$IS_BLUE_EXIST" ]; then
  CURRENT_PROFILE="blue"
  PROFILE="green"
else
  CURRENT_PROFILE="green"
  PROFILE="blue"
fi

echo "> 현재 프로필: $CURRENT_PROFILE"
echo "> 다음 프로필: $PROFILE"
echo ""

echo "> 미사용 도커 이미지 삭제"
docker image prune -f

echo "> 레디스 확인"
if [ -z "$IS_REDIS_EXIST" ]; then
  docker compose -f "${COMPOSE_FILE}" pull redis
  docker compose -f "${COMPOSE_FILE}" up -d redis
fi

echo "> ${PROFILE} 컨테이너 이미지 pull 및 실행"
docker compose -f "${COMPOSE_FILE}" pull "${PROFILE}"
docker compose -f "${COMPOSE_FILE}" up -d "${PROFILE}"

echo "> 헬스체크 시작"
if [ "$PROFILE" == "blue" ]; then
    HEALTH_CHECK_PORT=9091
else
    HEALTH_CHECK_PORT=9092
fi
echo "> 포트: $HEALTH_CHECK_PORT"

RETRY_COUNT=1
MAX_RETRY_COUNT=24

while [ $RETRY_COUNT -le $MAX_RETRY_COUNT ]
do
    echo "> 헬스체크 진행중 ($RETRY_COUNT / $MAX_RETRY_COUNT)"

    if curl -s http://127.0.0.1:${HEALTH_CHECK_PORT}/actuator/health | grep -q "UP"; then
        echo "> 헬스체크 성공"
        break
    fi

    if [ $RETRY_COUNT -eq $MAX_RETRY_COUNT ]; then
        echo "> 헬스체크 실패, ${PROFILE} 컨테이너 정지 후 배포 중단"
        docker compose -f "${COMPOSE_FILE}" stop "${PROFILE}"
        exit 1
    fi

    sleep 5
    RETRY_COUNT=$((RETRY_COUNT+1))
done
echo ""

echo "> Nginx 트래픽 전환"
sudo ln -sf /etc/nginx/conf.d/service-${PROFILE}.inc /etc/nginx/conf.d/service-env.inc
sudo nginx -s reload
echo "> 트래픽 전환 완료"
echo ""

if [ "$CURRENT_PROFILE" != "none" ]; then
    echo "> 이전 서버 종료($CURRENT_PROFILE)"
    docker compose -f "${COMPOSE_FILE}" stop "${CURRENT_PROFILE}"
fi

echo "> 배포 완료"

exit 0
