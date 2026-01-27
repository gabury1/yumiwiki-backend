# 멀티 스테이지 빌드: 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Gradle 래퍼 및 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY yumiwiki/build.gradle yumiwiki/

# 의존성 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY yumiwiki/src yumiwiki/src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle :yumiwiki:bootJar --no-daemon -x test

# 런타임 스테이지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 타임존 설정 (Asia/Seoul)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 애플리케이션 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=builder /app/yumiwiki/build/libs/*.jar app.jar

# 데이터베이스 디렉토리 볼륨
VOLUME /app/database

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]