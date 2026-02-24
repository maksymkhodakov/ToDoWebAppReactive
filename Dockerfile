FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /ToDoWebApp
COPY . .
RUN mvn -f pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-jammy
WORKDIR /ToDoWebApp
COPY --from=builder /ToDoWebApp/target/*.jar /ToDoWebApp/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/ToDoWebApp/*.jar"]
