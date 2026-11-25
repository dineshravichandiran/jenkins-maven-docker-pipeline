# Runtime image only: the jar is built and tested by Maven before this step (see buildspec.yml).
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} PORT=8080
RUN useradd --system --uid 10001 --no-create-home appuser
WORKDIR /opt/app
COPY target/greeter-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD ["java", "-version"]
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
