FROM eclipse-temurin:17
WORKDIR /app
COPY springboot-demo/target/.*jar springboot-demo.jar
EXPOSE 8080
CMD ["java", "'jar", "springboot-demo.jar"]