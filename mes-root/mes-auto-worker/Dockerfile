FROM mcr.microsoft.com/java/jre:11u3-zulu-alpine

ENV TZ Asia/Shanghai
ENV JAVA_OPTIONS ""
ENV VERTICLE_HOME /usr/verticles
ENV VERTICLE_FILE mes-auto-worker-fat.jar
COPY $VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED $JAVA_OPTIONS -jar $VERTICLE_FILE"]