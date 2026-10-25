FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xmx384m"
COPY pom.xml .
COPY .mvn ./.mvn
COPY src ./src
RUN --mount=type=cache,id=lilith-bloom-maven,target=/root/.m2 mvn --batch-mode --no-transfer-progress verify
FROM tomcat:9.0-jdk17-temurin
COPY --from=build /app/target/lilith-bloom.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
