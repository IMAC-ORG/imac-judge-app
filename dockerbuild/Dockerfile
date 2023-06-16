FROM openjdk:17-alpine
VOLUME /tmp
ARG JAR_FILE=judge-0.0.1-SNAPSHOT.jar
RUN echo ${JAR_FILE}
EXPOSE 8080
COPY ${JAR_FILE} app.jar
COPY settings.json /var/opt/judge/settings.json
ENTRYPOINT ["java","-jar","/app.jar"]