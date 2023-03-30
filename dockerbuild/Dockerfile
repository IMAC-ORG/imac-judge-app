FROM openjdk:17-alpine
VOLUME /tmp
ARG JAR_FILE
EXPOSE 8080
COPY judge-0.0.1-SNAPSHOT.jar app.jar
COPY pilots.dat /tmp/pilots.dat
COPY sequences.dat /tmp/sequences.dat
ENTRYPOINT ["java","-jar","/app.jar"]