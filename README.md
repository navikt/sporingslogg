
# Team Samhandling Springslogg

[![Build](https://github.com/navikt/sporingslogg/workflows/bygg_og_deploy_q2/badge.svg)](https://github.com/navikt/sporingslogg/actions)
[![Build](https://github.com/navikt/sporingslogg/workflows/Deploy%20to%20production/badge.svg)](https://github.com/navikt/sporingslogg/actions)
![](https://github.com/navikt/eessi-pensjon-fagmodul/workflows/Deploy%20to%20production/badge.svg)
![](https://github.com/navikt/eessi-pensjon-fagmodul/workflows/deploy_to_production/badge.svg)


### Er en Kotlin Spring Boot applikasjon for team Pensjonsamhandling.

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
