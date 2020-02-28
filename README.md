# Revolut Recruitment Task

### Requirements

Java 11+

### Build and run

Run as Docker image:

```
./build/build-and-run.sh
```

Build and run with Maven:

```
./mvnw clean package

java -jar target/revolut-recruitment-task-1.0-SNAPSHOT-jar-with-dependencies.jar

```

## Api description:

Please use `Revolut.postman_collection.json` to explore the API. Here is a list of endpoints in the app:

### Account

- /v1/account - POST
- /v1/account/:id - PUT
- /v1/account/:id/balance - GET
- /v1/account/list - GET

### Transaction

- /v1/transaction/transfer - POST
- /v1/transaction/top-up - POST
- /v1/transaction/list/:id - GET

