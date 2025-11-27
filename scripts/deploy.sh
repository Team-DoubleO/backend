#!/bin/bash

# =================================================================
#  Retrip 프로젝트 배포 및 SSL 인증서 자동화 스크립트
# =================================================================

echo "환경 설정을 시작합니다..."
if [ ! -f .env ]; then
    echo ".env 파일이 없습니다. 스크립트를 중단합니다."
    exit 1
fi

export $(grep -v '^#' .env | xargs)

MAIN_DOMAIN="sspots.site"
CERT_FILE_PATH="./data/certbot/conf/live/$MAIN_DOMAIN/fullchain.pem"
NGINX_CONF_DIR="./nginx/conf.d"
NGINX_CONTAINER_NAME="nginx"
WHITELIST_FILE="$NGINX_CONF_DIR/allowed_ips.rules"

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

check_certificate() {
    # sudo 권한으로 파일 존재 확인
    if sudo [ -f "$CERT_FILE_PATH" ]; then
        echo "기존 SSL 인증서를 찾았습니다: $CERT_FILE_PATH"

        # sudo 권한으로 openssl 실행
        if sudo openssl x509 -checkend 2592000 -noout -in "$CERT_FILE_PATH" > /dev/null 2>&1; then
            echo "인증서가 유효합니다. (30일 이상 남음)"
            return 0
        else
            echo "인증서가 30일 이내에 만료됩니다. 갱신이 필요합니다."
            return 1
        fi
    else
        echo "SSL 인증서가 존재하지 않습니다."
        return 1
    fi
}

renew_certificate() {
    echo "인증서 갱신을 시도합니다..."
    $DOCKER_COMPOSE run --rm certbot renew

    if [ $? -eq 0 ]; then
        echo "인증서 갱신 성공!"
        return 0
    else
        echo "인증서 갱신 실패. 새로 발급을 시도합니다."
        return 1
    fi
}

issue_new_certificate() {
    echo "새로운 SSL 인증서 발급을 시작합니다."

    echo "인증서 발급을 위해 임시 Nginx 설정을 적용합니다."
    sudo cp ./nginx-cert-setup.conf $NGINX_CONF_DIR/default.conf

    $DOCKER_COMPOSE up -d nginx

    echo "Nginx가 시작될 때까지 10초 대기합니다..."
    sleep 10

    echo "Certbot으로 SSL 인증서를 요청합니다..."
    $DOCKER_COMPOSE run --rm certbot certonly \
      --webroot --webroot-path=/var/www/certbot \
      -d sspots.site \
      -d www.sspots.site \
      --email $CERTBOT_EMAIL --agree-tos --no-eff-email

    if [ $? -ne 0 ]; then
        echo "SSL 인증서 발급에 실패했습니다."
        echo "Let's Encrypt 발급 제한에 걸렸을 가능성이 있습니다."
        echo "다음 중 하나를 시도해보세요:"
        echo "1. 기존 인증서 파일을 수동으로 복사"
        echo "2. 발급 제한 해제까지 대기"
        echo "3. 스테이징 환경에서 테스트"

        $DOCKER_COMPOSE logs nginx
        return 1
    fi

    echo "SSL 인증서 발급 성공!"
    $DOCKER_COMPOSE down
    return 0
}


if check_certificate; then
    echo "기존 인증서를 사용합니다."
else
    echo "인증서 처리가 필요합니다."

    if [ -f "$CERT_FILE_PATH" ]; then
        if ! renew_certificate; then
            echo "갱신 실패. 새 인증서 발급을 건너뜁니다."
            echo "기존 인증서를 그대로 사용합니다."
        fi
    else
        if ! issue_new_certificate; then
            echo "인증서 발급 실패. HTTP로 서비스를 시작합니다."
            echo "수동으로 인증서를 설정한 후 다시 배포하세요."
        fi
    fi
fi

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

echo "================================================================="
echo "배포가 성공적으로 완료되었습니다!"
echo "서비스 URL: https://$MAIN_DOMAIN"
echo "================================================================="