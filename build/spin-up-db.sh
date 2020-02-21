#!/usr/bin/env bash

docker run --name revolut-rec-task-postgres -p 5432:5432 -e POSTGRES_USER=revolut -e POSTGRES_PASSWORD=revolut_pass -d postgres

../mvnw -f ../ compile flyway:migrate
