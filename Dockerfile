FROM hmcts/cnp-java-base:openjdk-8u181-jre-alpine3.8-1.0

MAINTAINER "HMCTS Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Team <https://github.com/hmcts>"

ENV APP continuous-online-hearing.jar
ENV APPLICATION_TOTAL_MEMORY 2048M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 70

COPY build/libs/$APP /opt/app/

ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7005"

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005
EXPOSE 7005 7005
