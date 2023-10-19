FROM ghcr.io/navikt/baseimages/temurin:21

COPY nais/export-vault-secrets.sh /init-scripts/
COPY nais/jvm-tuning.sh /init-scripts/

COPY build/libs/sporingslogg.jar /app/app.jar
