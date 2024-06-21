FROM maven:3.8.4-openjdk-17 as builder

COPY . /app
WORKDIR /app

RUN mvn package -B -Dmaven.test.skip=true

FROM openjdk:17
COPY --from=builder /app/target/*SNAPSHOT.jar /app.jar

EXPOSE 8081

CMD ["java", "-Xms512m", "-Xmx4096m", "-jar", "/app.jar"]