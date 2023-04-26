# Reactive programming meetup (new) Spring WebFlux & Reactor demo service

## Futtatás

A demóalkalmazás futtatásához egy MS SQL Server adatbázisra van szükség, amelynek az elérési paraméterei az
application.properties fájlban találhatóak:

```properties
spring.datasource.url=r2dbc:postgresql://localhost:5432/rxjava
spring.datasource.username=SA
spring.datasource.password=Pass1234
```

## Adatbázis

A szolgáltatás működéséhez az alábbi adatbázis struktúra létrehozására van szükség

```sql
CREATE
DATABASE rxjava;

CREATE TABLE IF NOT EXISTS financial_transaction
(
    transaction_id char
(
    36
) PRIMARY KEY,
    previous_transaction_id char
(
    36
) NULL,
    data bytea NOT NULL
    );

CREATE TABLE IF NOT EXISTS archive_financial_transaction
(
    account_statement_id CHAR
(
    36
) NOT NULL,
    transaction_id CHAR
(
    36
) NOT NULL,
    transaction_number INT NOT NULL,
    compressed_data bytea NOT NULL,
    signature bytea NOT NULL,
    CONSTRAINT PK_ArchiveFinancialTransaction
    PRIMARY KEY
(
    account_statement_id,
    transaction_id
)
    );

CREATE UNIQUE INDEX archive_financial_transaction_transaction_id_key
    ON archive_financial_transaction (transaction_id);
```

## Tesztadat

Amennyiben a FinancialTransaction tábla üres, az feltöltésre kerül a **másik, Sring MVC-s** szolgáltatás által (annak
indulásakor)
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

## Teszteredmények blocking vs non-blocking

Tesztkörnyezet

Hardware: 2021 MacBook Pro 16" (M1 Pro, 32GB RAM) - 10-core CPU (8 performance cores and 2 efficiency cores)

OS: macOS Ventura 13.3.1

JVM: OpenJDK Runtime Environment Temurin-17.0.1+12 (build 17.0.1+12)

### Baseline (blocking results)

1. 22.83s
2. 18.91s
3. 23.21s
4. 27.95s
5. 19.59s
6. 17.67s
7. 23.05s
8. 26.76s

AVG: 22.49625s

### Non-blocking

1. 8s
2. 10.13s
3. 10.55s
4. 14.24s
5. 15.69s
6. 16.40s
7. 14.77s
8. 9.94s

AVG: 12.465s

### Összegzés

**A non-blocking (párhuzamosított) verzió 1.8x gyorsabb mint a blokcing verzió.**

Ugyanakkor, az R2DBC általánosságban lassabban teljesít, mint a JDBC. Ezért a teljesítmény növekedés még nagyobb
lehetne, ha egy kellően optimalizált R2DBC adatbázis drivert tudnánk használni.
