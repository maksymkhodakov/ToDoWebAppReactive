FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /ToDoWebApp
COPY . .
RUN mvn -f pom.xml clean package -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim
WORKDIR /ToDoWebApp
COPY --from=builder /ToDoWebApp/target/*.jar /ToDoWebApp/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/ToDoWebApp/*.jar"]
