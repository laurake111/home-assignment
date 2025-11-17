FROM eclipse-temurin:24-jdk-noble
WORKDIR /app

COPY . .

RUN ./gradlew bootJar

ENTRYPOINT ["./gradlew"]
CMD ["bootRun"]