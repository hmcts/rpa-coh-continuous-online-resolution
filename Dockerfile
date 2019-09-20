ARG APP_INSIGHTS_AGENT_VERSION=2.3.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.1

MAINTAINER "HMCTS Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Team <https://github.com/hmcts>"

WORKDIR /opt/app
COPY build/libs/continuous-online-hearing.jar .

ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7005"

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005
EXPOSE 7005 7005

ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/continuous-online-hearing.jar"
