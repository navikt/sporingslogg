FROM navikt/java:17

COPY build/libs/sporingslogg.jar /app/app.jar
COPY nais/export-vault-secrets.sh /init-scripts/
