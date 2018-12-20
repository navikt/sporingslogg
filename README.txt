
Se Fitnesse-tester for brukstilfeller og mulige tjenestekall

Testing lokalt: 
- se StandaloneTestJettyMain. 
  Kan konfigurere både ldap, oidc-provider, kafka og database til mer eller mindre realistiske utgaver.
  Fitnesse-testene som kjører som enhetstester bruker en enkel standalone-variant.

Testing med "lokal" Docker:
- bruk Dockerfile_uten_nais, f.eks docker image build -t sporingslogg:latest -f Dockerfile_uten_nais . ; docker container run -p 8088:8088 <image>
  Kan bl.a. testes med Fitnesse-tester for ekstern-server
  NB: Kan bli problemer med SSL til f.eks. kafka, hvis ikke Docker-serveren klarer dette
  
Testing med NAIS:
- Fitnesse-tester for ekstern-server


------------------- Manuelle operasjoner pr miljø/prodsetting:

- opprette DB i Basta (se db_ddl.sql under resources i core-prosjekt)
- opprette srvsporingslogg i Basta
- opprette topic++ i Kafka, oneShot-json:
{ "topics": [ { "configEntries": {},
      "members": [
        { "member": "srvsporingslogg", "role": "PRODUCER" },
        { "member": "srvsporingslogg", "role": "CONSUMER" }
      ], "numPartitions": 1,
      "topicName": "aapen-sporingslogg-loggmeldingMottatt"
    } ] }
- opprette baseurl (?) for topic i Fasit, url = topicnavn
- brukere som skal kunne POSTe til kafka/restapi må i gruppe KP-<topicnavn>
- brukere som skal kunne GETe egne logger må ha OIDC-token fra oppsatt provider
