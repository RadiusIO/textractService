FROM adoptopenjdk:11-jre-hotspot as build-des
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk:11-jre-hotspot
EXPOSE 8083
COPY --from=build-des dependencies/ ./
COPY --from=build-des snapshot-dependencies/ ./
COPY --from=build-des spring-boot-loader/ ./
COPY --from=build-des application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
