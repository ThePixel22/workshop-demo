FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG SERVICE
WORKDIR /workspace

COPY pom.xml ./
COPY member-service/pom.xml member-service/pom.xml
COPY redemption-service/pom.xml redemption-service/pom.xml
COPY member-service/src member-service/src
COPY redemption-service/src redemption-service/src

RUN mvn -pl "${SERVICE}" -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

ARG SERVICE
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/${SERVICE}/target/*.jar app.jar

USER spring:spring
EXPOSE 8061 8062

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
