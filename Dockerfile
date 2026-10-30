# Etapa 1: compilar el jar con el JDK completo.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
# Las pruebas usan Testcontainers (necesitan Docker), así que aquí se omiten.
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: imagen final solo con el JRE y el jar.
FROM eclipse-temurin:25-jre
COPY --from=build /app/build/libs/crm-api-0.0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
