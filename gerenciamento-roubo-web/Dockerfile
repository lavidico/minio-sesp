FROM node:12.13-alpine AS build
RUN mkdir /teste
WORKDIR /app
COPY package*.json /app/
RUN npm install --loglevel verbose
COPY src /app/src/
COPY *.json /app/
COPY *.js /app/
RUN npm run build


FROM nginx:latest
VOLUME /var/cache/nginx
EXPOSE 8080
COPY --from=build /app/dist/gerenciamento-roubo-web /usr/share/nginx/html
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf
