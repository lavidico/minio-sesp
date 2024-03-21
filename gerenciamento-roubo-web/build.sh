#!/bin/bash
# Executa build e cria uma imagem aplicação gerenciamento roubo web utilizando a tag
# br.gov.mt.sesp/gerenciamento-roubo-web
docker build -t br.gov.mt.sesp/gerenciamento-roubo-web --no-cache .
