# 1️⃣ 빌드 단계: Gradle을 사용하여 JAR 파일 생성
FROM eclipse-temurin:17-jdk as build
WORKDIR /app

# 프로젝트 소스 코드 복사
COPY . .

# gradlew 실행 권한 추가
RUN chmod +x ./gradlew

# Gradle 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test

# 3️⃣ 실행 컨테이너 생성
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 4️⃣ Build Args (빌드 시 전달됨)
ARG MONGO_DB_URI
ARG MONGO_DB_NAME
ARG DISCORD_WEBHOOK_URL
ARG SERVER_PORT

# 5️⃣ ENV 설정 (컨테이너 내부에서도 접근 가능)
ENV MONGO_DB_URI=$MONGO_DB_URI
ENV MONGO_DB_NAME=$MONGO_DB_NAME
ENV DISCORD_WEBHOOK_URL=$DISCORD_WEBHOOK_URL
ENV SERVER_PORT=$SERVER_PORT

# 6️⃣ JAR 파일 복사 및 실행
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]