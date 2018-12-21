FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.2

MAINTAINER "HMCTS Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Team <https://github.com/hmcts>"
#
#WORKDIR /opt/app
#COPY build/libs/continuous-online-hearing.jar .

# Mandatory!
ENV APP continuous-online-hearing.jar
ENV APPLICATION_TOTAL_MEMORY 512M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 53

#ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7005"

#HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health
ENV JAVA_OPTS ""

#EXPOSE 8080 5005
#EXPOSE 7005 7005

COPY build/libs/$APP /opt/app/

#ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/continuous-online-hearing.jar"