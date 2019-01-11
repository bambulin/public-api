# docker build -t whalebone/public-api .
FROM ubuntu:16.04
MAINTAINER Tomas Kozel <kozelto@gmail.com>
LABEL description="Whalebone public-api server"

ENV DEPS openjdk-8-jdk wget unzip git maven
ENV JBOSS_HOME "/opt/whalebone/wildfly"
ENV JAVA_HOME "/usr/lib/jvm/java-8-openjdk-amd64"

RUN apt-get update \
    && apt-get install -y $DEPS
RUN useradd -m -s /sbin/nologin whalebone
RUN mkdir -p /opt/whalebone && chown whalebone /opt/whalebone && chgrp whalebone /opt/whalebone \
    && chmod ug+rwxs /opt/whalebone

WORKDIR /opt/whalebone

EXPOSE 8080

ENV WILDFLY_VERSION 14.0.1.Final
ENV WF_CONFDIR /opt/whalebone/wildfly/standalone/configuration
ENV WF_CONFIG ${WF_CONFDIR}/standalone.xml

RUN \
    wget http://download.jboss.org/wildfly/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.zip && \
    unzip wildfly-${WILDFLY_VERSION}.zip && rm -rf wildfly-${WILDFLY_VERSION}.zip && \
    # Workaround for https://github.com/docker/docker/issues/5509
    ln -s /opt/whalebone/wildfly-${WILDFLY_VERSION} /opt/whalebone/wildfly

ADD public-api.sh /opt/whalebone/
RUN chown whalebone /opt/whalebone/public-api.sh && chgrp whalebone /opt/whalebone/public-api.sh && chmod u+x /opt/whalebone/public-api.sh

RUN chown -R whalebone /opt/whalebone/ && chgrp -R whalebone /opt/whalebone/

USER whalebone
ADD ear/target/public-api.ear /opt/whalebone/wildfly-${WILDFLY_VERSION}/standalone/deployments/

RUN echo 'JAVA_OPTS="\
 -server \
 -Xms${MS_RAM:-1g} \
 -Xmx${MX_RAM:-1g} \
 -XX:MetaspaceSize=64m \
 -XX:MaxMetaspaceSize=512m \
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=100 \
 -XX:InitiatingHeapOccupancyPercent=70 \
 -XX:+HeapDumpOnOutOfMemoryError \
 -XX:HeapDumpPath=/opt/whalebone \
"' >> /opt/whalebone/wildfly/bin/standalone.conf

CMD ["/opt/whalebone/public-api.sh"]
