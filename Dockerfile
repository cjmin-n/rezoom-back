# 1️⃣ 첫 번째 스테이지: Gradle을 사용하여 빌드
FROM eclipse-temurin:17-jdk as build
WORKDIR /app

# 프로젝트 소스 코드 복사
COPY . .

# Gradle로 프로젝트 빌드 (테스트 제외)
RUN ./gradlew clean build -x test

# 2️⃣ 두 번째 스테이지: 실행을 위한 최적화된 이미지
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]
