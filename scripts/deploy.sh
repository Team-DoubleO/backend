#!/bin/bash

# =================================================================
#  FitFinder 프로젝트 배포 및 SSL 인증서 자동화 스크립트
# =================================================================
echo "환경 설정을 시작합니다..."
if [ ! -f .env ]; then
    echo ".env 파일이 없습니다. 스크립트를 중단합니다."
    exit 1
fi

export $(grep -v '^#' .env | xargs)

if [ -z "$DISCORD_WEBHOOK_URL" ]; then
    echo "❌ DISCORD_WEBHOOK_URL 이 설정되지 않았습니다."
    exit 1
fi

MAIN_DOMAIN="sspots.site"
CERT_FILE_PATH="./data/certbot/conf/live/$MAIN_DOMAIN/fullchain.pem"
NGINX_CONF_DIR="./nginx/conf.d"
NGINX_CONTAINER_NAME="nginx"
WHITELIST_FILE="$NGINX_CONF_DIR/allowed_ips.rules"

DISK_WARN_THRESHOLD=80
DISK_CLEAN_THRESHOLD=90

REQUIRED_DOMAINS=(
  sspots.site
  www.sspots.site
  api.sspots.site
  grafana.sspots.site
  prometheus.sspots.site
)

send_discord() {
  local TITLE="$1"
  local BODY="$2"

  if [ -z "$DISCORD_WEBHOOK_URL" ]; then
    echo "DISCORD_WEBHOOK_URL is empty"
    return 1
  fi

  local payload
  payload=$(jq -nc \
    --arg content "**${TITLE}**

${BODY}" \
    '{content: $content}')

  if ! curl --fail -sS \
    -H "Content-Type: application/json" \
    -d "$payload" \
    "$DISCORD_WEBHOOK_URL"; then
    echo "Discord notify failed"
    return 1
  fi

  return 0
}

get_root_disk_usage() {
    df / | awk 'NR==2 {print $5}' | sed 's/%//'
}

get_root_disk_status() {
    df -h | grep "/dev/root"
}

get_docker_image_status() {
    docker system df | sed '1d'
}

notify_disk_status() {
    local USAGE="$1"

    send_discord "📊 서버 디스크 상태 리포트" \
"🖥️ **EC2 Root Disk**
\`\`\`
$(get_root_disk_status)
\`\`\`

🐳 **Docker Images**
\`\`\`
$(get_docker_image_status)
\`\`\`

📈 **Disk Usage**: **${USAGE}%**"
}

handle_disk_overflow() {
    local USAGE="$1"

    send_discord "🚨 디스크 임계치 초과" \
"❌ 디스크 사용량이 **${USAGE}%** 를 초과했습니다.

🧹 **자동 복구 진행**
- docker compose down
- docker system prune -a --volumes
- 이미지 재배포 시작"

    $DOCKER_COMPOSE down
    docker system prune -a --volumes -f
    $DOCKER_COMPOSE pull spots-app
    $DOCKER_COMPOSE up -d --force-recreate

    send_discord "✅ 디스크 정리 및 재배포 완료" \
"📦 **정리 후 Disk**
\`\`\`
$(get_root_disk_status)
\`\`\`

🐳 **Docker 상태**
\`\`\`
$(get_docker_image_status)
\`\`\`"
}

check_disk_and_notify() {
    local USAGE
    USAGE=$(get_root_disk_usage)

    notify_disk_status "$USAGE"

    if [ "$USAGE" -ge "$DISK_CLEAN_THRESHOLD" ]; then
        handle_disk_overflow "$USAGE"
    elif [ "$USAGE" -ge "$DISK_WARN_THRESHOLD" ]; then
        send_discord "⚠️ 디스크 사용량 경고" \
"📈 현재 디스크 사용량이 **${USAGE}%** 입니다.
조만간 Docker 이미지 정리가 필요할 수 있습니다."
    fi
}

if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
elif docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    echo "Docker Compose를 찾을 수 없습니다! 스크립트를 중단합니다."
    exit 1
fi

echo "Docker Compose 명령어: $DOCKER_COMPOSE"
sudo mkdir -p $NGINX_CONF_DIR
sudo mkdir -p ./data/certbot/conf
sudo mkdir -p ./data/certbot/www

check_certificate_domains() {
  local cert="$CERT_FILE_PATH"

  echo "🔍 인증서 SAN 검사 중..."

  local san_list
  san_list=$(sudo openssl x509 -in "$cert" -noout -ext subjectAltName | tr ',' '\n')

  for domain in "${REQUIRED_DOMAINS[@]}"; do
    if ! echo "$san_list" | grep -q "DNS:$domain"; then
      echo "❌ SAN 누락: $domain"
      return 1
    fi
  done

  echo "✅ 모든 도메인이 인증서에 포함되어 있습니다."
  return 0
}

check_certificate_validity() {
  if sudo openssl x509 -checkend 2592000 -noout -in "$CERT_FILE_PATH"; then
    echo "✔ 인증서 유효 기간 정상 (30일 이상)"
    return 0
  else
    echo "⚠ 인증서 만료 임박"
    return 1
  fi
}

renew_certificate() {
  echo "🔄 인증서 갱신 시도"
  $DOCKER_COMPOSE run --rm certbot renew
}

issue_new_certificate() {
  echo "🆕 인증서 신규/재발급(certonly) 시작"

  echo "➡ 발급용 nginx 설정 적용"
  cp ./nginx-cert-setup.conf "$NGINX_CONF_DIR/default.conf"

  $DOCKER_COMPOSE up -d nginx
  sleep 8

  $DOCKER_COMPOSE run --rm certbot certonly \
    --webroot -w /var/www/certbot \
    --force-renewal \
    -d sspots.site \
    -d www.sspots.site \
    -d api.sspots.site \
    -d grafana.sspots.site \
    -d prometheus.sspots.site \
    --email "$CERTBOT_EMAIL" \
    --agree-tos \
    --no-eff-email

  echo "✅ 인증서 발급 완료"
}

if [ -f "$CERT_FILE_PATH" ]; then
  echo "🔐 기존 SSL 인증서를 찾았습니다: $CERT_FILE_PATH"

  if ! check_certificate_domains; then
    echo "🚨 SAN 누락 → 인증서 재발급 필요"
    issue_new_certificate
  elif ! check_certificate_validity; then
    echo "⏰ 만료 임박 → 갱신 시도"
    renew_certificate
  else
    echo "✅ 인증서 유효 + 도메인 구성 정상"
    echo "기존 인증서를 사용합니다."
  fi
else
  echo "❌ 인증서 없음 → 신규 발급"
  issue_new_certificate
fi

# ===============================================================
# 🔍 배포 전 디스크 체크
# ===============================================================
check_disk_and_notify

echo "최종 운영 설정을 적용하고 모든 서비스를 시작합니다."

echo "운영용 Nginx 설정을 적용합니다."
sudo cp ./nginx-prod.conf $NGINX_CONF_DIR/default.conf

echo "기존 컨테이너를 종료합니다..."
$DOCKER_COMPOSE down

echo "새로운 Docker 이미지를 pull 합니다"
$DOCKER_COMPOSE pull spots-app

echo "모든 서비스를 시작/업데이트합니다..."
$DOCKER_COMPOSE up -d

echo "Nginx 설정을 리로드합니다..."
$DOCKER_COMPOSE exec $NGINX_CONTAINER_NAME nginx -s reload

send_discord "🎉 배포 완료" \
"✅ 최신 이미지로 서비스가 배포되었습니다.

🌐 **Service URL**
https://${MAIN_DOMAIN}"

echo "================================================================="
echo "배포가 성공적으로 완료되었습니다!"
echo "서비스 URL: https://$MAIN_DOMAIN"
echo "================================================================="