# compila con maven
FROM maven:3.9-eclipse-temurin-17 AS compilar
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ejecuta el jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=compilar /app/target/alojamientos-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
