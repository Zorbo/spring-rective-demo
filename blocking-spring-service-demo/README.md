# Reactive programming meetup (classic) Spring MVC demo service

## Webinar

Az előadásról készült felvétel az alábbi linken érhető el.

https://youtu.be/HITgoguM3hI

Az előadásban bemutattot Akka demo service az alábbi linken érhető el.

https://github.com/ZoltanLaszlo/non-blocking-akka-service-demo

## Futtatás

A demóalkalmazás futtatásához egy MS SQL Server adatbázisra van szükség, amelynek az elérési paraméterei az
application.properties fájlban találhatóak:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;database=RXJAVA;encrypt=false
spring.datasource.username=SA
spring.datasource.password=Pass1234
```

## Adatbázis

A szolgáltatás működéséhez az alábbi adatbázis struktúra létrehozására van szükség

```sql
CREATE DATABASE rxjava;

CREATE TABLE IF NOT EXISTS financial_transaction
(
    transaction_id          char(36) PRIMARY KEY,
    previous_transaction_id char(36) NULL,
    data                    bytea    NOT NULL
);

CREATE TABLE IF NOT EXISTS archive_financial_transaction
(
    account_statement_id CHAR(36) NOT NULL,
    transaction_id       CHAR(36) NOT NULL,
    transaction_number   INT      NOT NULL,
    compressed_data      bytea    NOT NULL,
    signature            bytea    NOT NULL,
    CONSTRAINT PK_ArchiveFinancialTransaction
        PRIMARY KEY (account_statement_id, transaction_id)
);

CREATE UNIQUE INDEX archive_financial_transaction_transaction_id_key
    ON archive_financial_transaction (transaction_id);
```

## Tesztadat

Amennyiben a FinancialTransaction tábla üres, az feltöltésre kerül a szolgáltatás által (annak indulásakor)
véletlenszerű adatokkal. A tranzakció láncok utolsó elemeinek azonosítóit a "top_transcations_ids.txt"
fájlba menti a szolgáltatás. Az itt felsorolt azonosítókkal lehet meghívni a "számlakivonat" generálást.

Lásd: BlockingSpringServiceDemoApplication.generateRandomData

## Tesztelés

Az alábbi REST API végpont hívással indítható el a szolgáltatás által végzett kivonatolás. Értelem szerűen a HTTP
body-ban meghatározott lastTranscationId értékét ki kell cserélni egy a "top_transactionIds.txt"-ben található UUID
egyikre. (Mindegyik UUID-re csak egyszer futtatható a "számlakivonat" generálás.)

```shell
curl --location --request POST 'http://localhost:8080/api/v1/account-service/accounts/statements' \
--header 'Content-Type: application/json' \
--data-raw '{
  "lastTransactionId": "3c3971df-7eaa-44da-8fd5-0dad16093a1d"
}'
```

A végpont OpenAPI leírója az oas.yml fájlban található.
(Megjegyzés.: A lekérdező GET-es végpont nem került megvalósításra. A teszt szempontjából csak a POST-os végpont
releváns.)