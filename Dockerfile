FROM eclipse-temurin:21-jre

COPY --chown=nobody:nogroup target/quarkus-app/lib/ /deployment/lib/
COPY --chown=nobody:nogroup target/quarkus-app/*.jar /deployment/
COPY --chown=nobody:nogroup target/quarkus-app/app/ /deployment/app/
COPY --chown=nobody:nogroup target/quarkus-app/quarkus/ /deployment/quarkus/

USER nobody
WORKDIR /

EXPOSE 8080
EXPOSE 8443

CMD exec java ${JAVA_OPTS} -jar /deployment/quarkus-run.jar
