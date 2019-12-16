FROM mcr.microsoft.com/java/jre:11u5-zulu-alpine

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
CMD ["exec java --add-modules java.se \
--add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
--add-opens java.base/java.lang=ALL-UNNAMED \
--add-opens java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens java.base/java.nio=ALL-UNNAMED \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.management/sun.management=ALL-UNNAMED \
--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
$JAVA_OPTIONS -jar $VERTICLE_FILE"]
