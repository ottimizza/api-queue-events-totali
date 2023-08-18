# # -----------------------------------------------------------------
# #### Stage 1: Build the application
FROM maven:3-openjdk-11-slim as build

# # Copy maven executable to the image
COPY pom.xml /build/
COPY src /build/src

# # Set the current working directory inside the image
WORKDIR /build

# # Build all the dependencies in preparation to go offline. 
# # This is a separate step so the dependencies will be cached unless 
# # the pom.xml file has changed.
# RUN mvn dependency:go-offline -B

# # Package the application
RUN mvn package -DskipTests
# RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# ####
# #### Stage 2: A minimal docker image with command to run the app 
FROM openjdk:11-jdk-slim

WORKDIR /app

# ARG DEPENDENCY=/app/target/dependency

# # Copy project dependencies from the build stage
COPY --from=build /build/target/tareffa-worker-0.0.1-SNAPSHOT.jar /app/
# COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
# COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# FROM openjdk:11-jdk-slim

VOLUME /tmp

# # -----------------------------------------------------------------
# Make port 8080 available to the world outside this container
# EXPOSE 9092
EXPOSE $PORT

# Run the jar file 
# java -Dgrails.env=prod -jar build/libs/api-framework-example-0.1.jar 
# ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/oauth2-service.jar"]
ENTRYPOINT ["java", "-Dserver.port=$PORT", "-Xmx256m", "-Xss512k", "-jar", "tareffa-worker-0.0.1-SNAPSHOT.jar"]
# -Xmx256m -Xss512k -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+UseConcMarkSweepGC