#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG BASE_IMAGE_VERSION
FROM armdocker.rnd.ericsson.se/proj-am/sles/sles-corretto-openjdk17:${BASE_IMAGE_VERSION}

ARG GIT_COMMIT=""
ARG APP_VERSION=""
ARG BUILD_TIME=""
ARG PRODUCT_REVISION=""
ARG NBI_DATA_DIR="/nbi"
# NBI_UID and NBI_GID were generated based on ADP rule DR-D1123-122.
ARG NBI_GID=241546
ARG NBI_UID=241546

ENV JAVA_OPTS ""

LABEL \
      com.ericsson.product-name="EVNFM NBI" \
      com.ericsson.product-number="CXU 101 0677" \
      com.ericsson.product-revision="R1A" \
      org.opencontainers.image.title="EVNFM NBI" \
      org.opencontainers.image.created=${BUILD_TIME} \
      org.opencontainers.image.revision=${GIT_COMMIT} \
      org.opencontainers.image.version=${APP_VERSION} \
      org.opencontainers.image.vendor="Ericsson"

ADD target/eric-eo-evnfm-nbi.jar eric-eo-evnfm-nbi.jar

COPY entryPoint.sh /entryPoint.sh

RUN echo "${NBI_UID}:x:${NBI_UID}:${NBI_GID}:nbi-user:/:/bin/false" >> /etc/passwd \
    && sed -i '/root/s/bash/false/g' /etc/passwd

RUN sh -c 'touch /eric-eo-evnfm-nbi.jar' \
    && chmod 755 /entryPoint.sh \
    && zypper install -l -y shadow util-linux unzip \
    && zypper clean --all \
    && mkdir -p "$NBI_DATA_DIR" \
    && chown -fR ${NBI_UID}:0 "$NBI_DATA_DIR" \
    && chmod -R g=u "$NBI_DATA_DIR" \
    && chmod 00755 $(find / -perm /4000 2>/dev/null)

RUN chown ${NBI_UID}:0 /var/lib/ca-certificates/java-cacerts \
    && chmod -R g=u /var/lib/ca-certificates/java-cacerts \
    && chmod 755 /var/lib/ca-certificates/java-cacerts

USER ${NBI_UID}:${NBI_GID}
WORKDIR $NBI_DATA_DIR
ENTRYPOINT ["sh", "-c", "/entryPoint.sh $JAVA_OPTS"]

EXPOSE 8888
