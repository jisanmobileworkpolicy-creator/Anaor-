FROM node:20-alpine
WORKDIR /app

# Copy package descriptors
COPY package.json ./
RUN npm install --production || true

# Copy backend server
COPY server.js ./

# Create persistent storage directories
RUN mkdir -p data uploads

# Copy initial data (products, update configuration)
COPY data/ ./data/

# Declare persistent volume mount points for cloud hostings
VOLUME ["/app/data", "/app/uploads"]

EXPOSE 3000
ENV SERVER_PORT=3000
ENV NODE_ENV=production

CMD ["node", "server.js"]
