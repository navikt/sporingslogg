FROM ghcr.io/navikt/baseimages/temurin:21

RUN curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
COPY nais/opentelemetry.sh /init-scripts/
COPY nais/export-vault-secrets.sh /init-scripts/
COPY nais/jvm-tuning.sh /init-scripts/

COPY build/libs/sporingslogg.jar /app/app.jar
