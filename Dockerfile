FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY nais/export-vault-secrets.sh /init-scripts/
COPY nais/jvm-tuning.sh /init-scripts/

COPY build/libs/sporingslogg.jar /app/app.jar
