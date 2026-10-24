# ============================================================
# Stage 1: Build - Maven + JDK 24 (compile release=24)
# ============================================================
FROM eclipse-temurin:24-jdk AS builder

WORKDIR /build

# Cache Maven dependencies
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source va build (skip tests; tests da xac nhan trong CI pipeline)
COPY src/ src/
RUN ./mvnw package -DskipTests -Dmaven.compiler.release=24 -q

# ============================================================
# Stage 2: Runtime - JRE 21 slim (nho hon, bao mat hon)
# ============================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

LABEL maintainer="UTE Sport - Nhom Triet-Tho-Dat"
LABEL project="Badminton_Management_Project"
LABEL version="1.0-RC1"

WORKDIR /app

# Tao user khong phai root de chay ung dung
RUN groupadd -r badminton && useradd -r -g badminton -s /bin/false appuser

# Sao chep JAR tu stage build
COPY --from=builder /build/target/Badminton_Management_Project-1.0.jar app.jar

# Tao thu muc uploads va logs
RUN mkdir -p /app/uploads /app/logs && chown -R appuser:badminton /app

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health 2>/dev/null || curl -f http://localhost:8080/ 2>/dev/null || exit 1

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
