# Development image: run the existing Spring Boot project mounted at /workspace.
FROM ubuntu:24.04

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
ENV DEBIAN_FRONTEND=noninteractive \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    MAVEN_OPTS="-XX:MaxRAMPercentage=75.0"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Java 17, Maven, Git, database client, and common development tools.
RUN apt-get update && apt-get install -y --no-install-recommends \
      openjdk-17-jdk maven default-mysql-client \
      curl ca-certificates gnupg git unzip zip jq \
      bash build-essential openssh-client python3 \
    && rm -rf /var/lib/apt/lists/*

# Stripe CLI: official Stripe Debian repository.
RUN curl -fsSL \
      https://packages.stripe.dev/api/security/keypair/stripe-cli-gpg/public \
      | gpg --dearmor -o /usr/share/keyrings/stripe.gpg \
    && echo 'deb [signed-by=/usr/share/keyrings/stripe.gpg] https://packages.stripe.dev/stripe-cli-debian-local stable main' \
      > /etc/apt/sources.list.d/stripe.list \
    && apt-get update && apt-get install -y --no-install-recommends stripe \
    && rm -rf /var/lib/apt/lists/*

# Keep user-specific credentials and Maven cache writable.
RUN useradd --create-home --shell /bin/bash developer \
    && install -d -o developer -g developer /workspace \
         /home/developer/.m2 \
         /home/developer/.config/stripe

ENV HOME=/home/developer
USER developer
WORKDIR /workspace
EXPOSE 8080

# Run Spring Boot automatically when the container is started.
CMD ["mvn", "spring-boot:run"]
