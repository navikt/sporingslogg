FROM navikt/java:8
LABEL maintainer="aage.kramarics@nav.no"

# Skal være satt til 4 (Bank-ID) når man går mot ID-porten, 0 ellers
ENV SPORINGSLOGG_CHECK_AUTHLEVEL="0"

COPY sporingslogg-web/target/sporingslogg-web.jar /app/app.jar

# Expose default http jetty port
EXPOSE 8088
