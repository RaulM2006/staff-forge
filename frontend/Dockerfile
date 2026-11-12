# Build the Angular application

FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first to leverage Docker layer caching
COPY package.json package-lock.json ./

# Clean install of dependencies
RUN npm ci

# Copy the rest of the frontend source code and build for production
COPY . .
RUN npm run build -- --configuration production

# Serve static files with Nginx
FROM nginx:1.27-alpine AS runtime

# Remove default Nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled Angular browser assets from the build stage
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
