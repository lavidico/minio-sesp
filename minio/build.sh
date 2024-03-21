#!/bin/bash
# Executa o build de um projeto Spring Boot e cria uma imagem
# da aplicação com a tag br.gov.mt.sesp/registro-geral:latest

docker build -t br.gov.mt.sesp/minio --no-cache .