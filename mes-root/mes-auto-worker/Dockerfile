FROM java:8-jre-alpine

ENV TZ Asia/Shanghai
ENV VERTICLE_HOME /usr/verticles
ENV VERTICLE_FILE mes-auto-worker-fat.jar
COPY $VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]