FROM eclipse-temurin:17-jdk-alpine
RUN adduser judge;echo 'judge:P@ssword1234$' | chpasswd
RUN mkdir -p /var/opt/judge; chown judge:judge /var/opt/judge
VOLUME /tmp
ARG JAR_FILE=judge.jar
RUN echo ${JAR_FILE}
EXPOSE 8080
USER judge
COPY --chown=judge:judge figures/ /var/opt/judge/figures/
COPY --chown=judge:judge judge/target/${JAR_FILE} /var/opt/judge/bin/${JAR_FILE}
COPY --chown=judge:judge dockerbuild/settings.json /var/opt/judge/settings.json
COPY --chown=judge:judge scripts/judge_update.sh /home/judge/judge_update.sh
COPY --chown=judge:judge scripts/fetch_update.sh /home/judge/fetch_update.sh
RUN mkdir -p /var/opt/judge/pilots/scores
ENTRYPOINT ["java","-jar","/var/opt/judge/bin/judge.jar"]