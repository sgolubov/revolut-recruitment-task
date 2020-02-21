#!/usr/bin/env bash

./mvnw clean package dockerfile:build

docker run --rm --name revolut-recruitment-task -p 8080:8080 revolut-recruitment-task:1.0-SNAPSHOT
