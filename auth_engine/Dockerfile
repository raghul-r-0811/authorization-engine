FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY auth_engine/build/libs/auth_engine-0.0.1-SNAPSHOT.jar auth_engine.jar
ENTRYPOINT ["java" , "-jar","auth_engine.jar"]