FROM eclipse-temurin:21-jre

WORKDIR /srv

COPY target/*.jar .
COPY docker/user-service.sh .

RUN chmod +x user-service.sh

EXPOSE 8189

ENTRYPOINT ["./user-service.sh"]