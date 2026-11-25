# compila angular
FROM node:20 AS compilar
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# configuración elegida
ARG CONFIGURACION=production
RUN npx ng build --configuration $CONFIGURACION

# sirve con nginx
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilar /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
