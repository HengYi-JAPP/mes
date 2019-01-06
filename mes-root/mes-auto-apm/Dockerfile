FROM java:8-jre-alpine

ENV TZ Asia/Shanghai
ENV VERTICLE_HOME /usr/verticles
ENV VERTICLE_FILE mes-auto-log-fat.jar
COPY $VERTICLE_FILE $VERTICLE_HOME/

EXPOSE 8080

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]