# Build image, incl. mvn build
FROM maven:latest as build-des
ARG JAR_FILE=target/*.jar
WORKDIR /root
COPY src /root/src
COPY pom.xml /root/
RUN mvn clean package
RUN java -Djarmode=layertools -jar ${JAR_FILE} extract

# ALTERNATIVE build image, requires mvn build to be done and jar exists
#FROM adoptopenjdk:11-jre-hotspot as build-des
#COPY ${JAR_FILE} application.jar
#RUN java -Djarmode=layertools -jar application.jar extract

# Build application image
FROM adoptopenjdk:11-jre-hotspot
EXPOSE 8083
COPY --from=build-des /root/dependencies/ ./
COPY --from=build-des /root/snapshot-dependencies/ ./
COPY --from=build-des /root/spring-boot-loader/ ./
COPY --from=build-des /root/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
