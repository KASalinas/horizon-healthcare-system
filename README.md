# Horizon Healthcare Information System

Horizon is an early Java 21 healthcare information system. Its first vertical
slice supports patient registration, lookup by medical-record number, and
encounter recording in memory.

## Build and test

```sh
./build.sh
```

## Run

```sh
./build.sh run
```

The sample data is fictional. This project is not yet suitable for real patient
data: authentication, authorization, audit logging, encryption, persistence,
backups, and healthcare interoperability are not implemented.
