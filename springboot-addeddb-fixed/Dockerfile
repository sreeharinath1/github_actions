FROM maven:3.9-eclipse-temurin-17 AS builD
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar springboot-demo.jar
EXPOSE 8080
CMD ["java", "-jar", "springboot-demo.jar"]

