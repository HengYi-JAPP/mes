FROM openjdk:11-jre-slim

ENV TZ Asia/Shanghai
ENV JAVA_OPTIONS ""
ENV VERTICLE_HOME /usr/verticles
ENV VERTICLE_FILE mes-auto-agent-fat.jar
COPY $VERTICLE_FILE $VERTICLE_HOME/

EXPOSE 8080
EXPOSE 9998
EXPOSE 9999

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java $JAVA_OPTIONS -jar $VERTICLE_FILE"]