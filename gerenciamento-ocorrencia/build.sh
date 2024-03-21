#!/bin/bash
# Executa o build de um projeto Spring Boot e cria uma imagem
# da aplicação com a tag br.gov.mt.sesp/carteira-funcional:latest

rm -fr ./target
./mvnw clean package
mv ./target/*.jar ./target/app.jar

docker build -t br.gov.mt.sesp/gerenciamento-ocorrencia --no-cache .