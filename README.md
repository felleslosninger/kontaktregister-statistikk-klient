#Kontaktregister Statistikk

Modul for å hente statistikk fra kontaktregister og legge inn i statistikkløsningen.
Bruker rest-apiet i kontaktregisteret for å trekke ut data, og legger det inn v.h.a. rest-apiet til statistikkløsningen.

##Første gangs oppsett
Etter at prosjektet er klonet, kjør

`mvn package`

Dette vil både lage jar-filen for prosjektet, Dockerfile og docker image.
Applikasjonen kan nå startes enten på localhost eller i en docker-container.

**localhost**
`java -jar ./target/ krr-statistikk-klient*.jar`

**Docker**
`mvn docker:run`
