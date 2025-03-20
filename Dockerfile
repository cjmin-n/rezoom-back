# 1️⃣ Java 17 기반으로 빌드
FROM eclipse-temurin:17-jdk as build
WORKDIR /app

# 2️⃣ 빌드 아티팩트 생성
COPY . .
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
