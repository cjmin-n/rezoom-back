# 1️⃣ 빌드 단계: Gradle을 사용하여 JAR 파일 생성
FROM eclipse-temurin:17-jdk as build
WORKDIR /app

# 프로젝트 소스 코드 복사
COPY . .

# gradlew 실행 권한 추가
RUN chmod +x ./gradlew

# Gradle 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test

# 2️⃣ 실행 단계: 빌드된 JAR 파일을 실행하는 경량 이미지
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너에서 Spring Boot 실행
CMD ["java", "-jar", "app.jar"]
