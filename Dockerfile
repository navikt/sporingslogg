FROM navikt/java:8
LABEL maintainer="aage.kramarics@nav.no"

# Passord ligger i angitt vault-fil (key=value format), med angitt key
ENV PASSWORD_FILE="/var/run/secrets/nais.io/vault/secrets.properties"

ENV LDAP_USERNAME="srvsporingslogg"
# vault: ENV LDAP_PASSWORD
# vault: ENV SPORINGSLOGGDB_PASSWORD
ENV NO_NAV_SPORINGSLOGG_KAFKA_USERNAME="srvsporingslogg"
# vault: ENV NO_NAV_SPORINGSLOGG_KAFKA_PASSWORD  
ENV NO_NAV_SPORINGSLOGG_KAFKA_TOPIC="aapen-sporingslogg-loggmeldingMottatt"
      
COPY sporingslogg-web/target/sporingslogg-web.jar /app/app.jar

# Expose default http jetty port
EXPOSE 8088
