
# Team Samhandling Springslogg

### Er en Kotlin Spring Boot applikasjon.

```
Tar imot LoggMeldinger (hendelser, restkall) fra tjenester som har hatt innsyn i brukeres historie.
Disse meldinger skal lagres og gjøres tilgjengelig for brukere når de logger inn i Nav.
```

* Lytter på kafka strøm og lagrer i db
* Rest tjeneste hvor bruker kan hente opp sine data fra db
* Rest tjeneste hvor systemer kan lagre i db (i stedet for kafka)


### Sporingslogg har følgende plan ut 2022.

* plan er å flytte til gcp.
* flytte db fra oracle fss til postgresql gcp
* plan kafka om å flytte til gcp. (aiven)


### kontakt oss på NAV slack: 
#pensjon_samhandling
