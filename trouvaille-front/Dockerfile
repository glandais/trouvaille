####
# Build stage
####
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

####
# Runtime stage with Nginx
####
FROM steebchen/nginx-spa:stable

# Copy the built application
COPY --from=build /app/dist/ /app

EXPOSE 80

CMD ["nginx"]