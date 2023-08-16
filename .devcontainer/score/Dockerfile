FROM lscr.io/linuxserver/webtop:latest
RUN sudo apk add openjdk17
RUN sudo mkdir /score && cd /score && wget -O score.jar https://imac.org.au/Score/Score-4.53-Release.jar
#ENTRYPOINT ["/init","java","-jar","/score/score.jar"]