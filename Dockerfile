FROM gcr.io/distroless/java25-debian13:nonroot
ENV LANG="nb_NO.UTF-8" LC_ALL="nb_NO.UTF-8" TZ="Europe/Oslo"

COPY nais/jvm-tuning.sh /init-scripts/

COPY build/libs/sporingslogg.jar /app/app.jar

CMD ["-jar", "/app/app.jar"]