FROM navikt/java:8
LABEL maintainer="aage.kramarics@nav.no"

COPY sporingslogg-web/target/sporingslogg-web.jar /app/app.jar

# Expose default http jetty port
EXPOSE 8088
