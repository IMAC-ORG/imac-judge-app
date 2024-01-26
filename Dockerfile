FROM openjdk:17-alpine
RUN adduser judge;echo 'judge:P@ssword1234$' | chpasswd
RUN mkdir -p /var/opt/judge; chown judge:judge /var/opt/judge
VOLUME /tmp
ARG JAR_FILE=app.jar
RUN echo ${JAR_FILE}
EXPOSE 8080
USER judge
COPY --chown=judge:judge figures/ /var/opt/judge/figures/
COPY --chown=judge:judge dockerbuild/${JAR_FILE} /var/opt/judge/bin/judge.jar
COPY --chown=judge:judge dockerbuild/settings.json /var/opt/judge/settings.json
RUN mkdir -p /var/opt/judge/pilots/scores
ENTRYPOINT ["java","-jar","/var/opt/judge/bin/judge.jar"]