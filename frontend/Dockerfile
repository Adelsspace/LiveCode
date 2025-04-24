FROM node:23-alpine AS build

COPY package.json .

RUN npm install

COPY . .

RUN npm run build

FROM nginx:1.27

COPY --from=build /dist /usr/share/nginx/html
COPY --from=build nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]