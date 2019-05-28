
Se Fitnesse-tester for brukstilfeller og mulige tjenestekall

Testing lokalt: 
- se StandaloneTestJettyMain. 
  Kan konfigurere både ldap, oidc-provider, kafka og database til mer eller mindre realistiske utgaver.
  Fitnesse-testene som kjører som enhetstester bruker en enkel standalone-variant.

Testing på NAIS:
- Fitnesse-tester for ekstern-server (start StartFitnesseSporingsLoggWeb, åpne http://localhost:9090 i nettleser)


------------------- Manuelle operasjoner pr miljø/prodsetting:

- opprette DB i Basta (Og opprettet tabeller etc - se db_ddl.sql under resources i core-prosjekt)
- opprette srvsporingslogg i Basta
- opprette topic++ i Kafka-adminapi, oneShot-json:
{ "topics": [ { "configEntries": {},
      "members": [
        { "member": "srvsporingslogg", "role": "PRODUCER" },               !! ble ikke laget i prod !
        { "member": "srvsporingslogg", "role": "CONSUMER" }
      ], "numPartitions": 1,
      "topicName": "aapen-sporingslogg-loggmeldingMottatt"
    } ] }
- opprette baseurl (?) for topic i Fasit, url = topicnavn
- brukere som skal kunne POSTe til kafka/restapi må i gruppe KP-<topicnavn>. Bruk Kafka-adminapi for å legge til bruker.
  PUT /api/v1/topics/aapen-sporingslogg-loggmeldingMottatt/groups     - add/remove members in topic groups. Only members in KM-{topicName} are authorized
  body: {
    "member": "srvXXXX",
    "operation": "ADD",
    "role": "PRODUCER"
  }
 
- brukere som skal kunne GETe egne logger må ha OIDC-token fra oppsatt provider
