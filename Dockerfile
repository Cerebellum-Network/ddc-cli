## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-native-image:20.2.0-java11 AS build
COPY gradlew /project/gradlew
COPY gradle /project/gradle
COPY build.gradle.kts /project/
COPY settings.gradle.kts /project/
COPY gradle.properties /project/
USER quarkus
WORKDIR /project
COPY src /project/src
RUN ./gradlew -b /project/build.gradle.kts buildNative
RUN ls
RUN ls /
RUN ls /project
RUN ls /project/src

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY --from=build /project/build/*-runner /work/application
RUN chmod 775 /work
ENTRYPOINT ["./application"]