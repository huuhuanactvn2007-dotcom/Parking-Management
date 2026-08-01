# --- Stage 1: Build React App ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
# Set API URL gọi qua Domain chính
ENV REACT_APP_API_URL=http://huan20.io.vn/api
RUN npm run build

# --- Stage 2: Serve bằng Nginx ---
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
# Copy config nginx tùy chỉnh
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
