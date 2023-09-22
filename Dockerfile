FROM amazoncorretto:21

COPY --chown=nobody:nobody target/quarkus-app/lib/ /deployment/lib/
COPY --chown=nobody:nobody target/quarkus-app/*.jar /deployment/
COPY --chown=nobody:nobody target/quarkus-app/app/ /deployment/app/
COPY --chown=nobody:nobody target/quarkus-app/quarkus/ /deployment/quarkus/

USER nobody
WORKDIR /

EXPOSE 8080

CMD [ "java", "-jar", "/deployment/quarkus-run.jar" ]
