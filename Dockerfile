# Build único que empacota o front Angular dentro do jar do Spring Boot.
# Um serviço só no Railway = uma URL, sem CORS e sem custo dobrado.

# ── 1. Front Angular ──────────────────────────────────────────────
FROM node:22.22-alpine AS front
WORKDIR /front

COPY frontend/package*.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build

# ── 2. Back Spring Boot (empacota o front como estático) ──────────
FROM maven:3.9-eclipse-temurin-21 AS back
WORKDIR /build

# Camada de dependências separada: só refaz o download quando o pom muda.
COPY backend/pom.xml backend/settings.xml ./
RUN mvn -B -s settings.xml dependency:go-offline

COPY backend/src ./src
COPY --from=front /front/dist/peticiona-web/browser/ ./src/main/resources/static/

RUN mvn -B -s settings.xml clean package

# ── 3. Runtime enxuto ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Não rodar como root.
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=back /build/target/*.jar app.jar

EXPOSE 8080

# Container costuma ter pouca RAM no Railway — a JVM precisa saber disso.
# MaxRAMPercentage respeita o limite do cgroup em vez de ver a RAM do host.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
