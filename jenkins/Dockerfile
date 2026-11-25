FROM jenkins/jenkins:lts-jdk17
USER root
# Docker CLI only (builds talk to the host Docker daemon through the mounted socket).
RUN apt-get update \
 && apt-get install -y --no-install-recommends docker-cli \
 && rm -rf /var/lib/apt/lists/* \
 && docker --version
USER jenkins
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt \
 && test "$(ls /usr/share/jenkins/ref/plugins | wc -l)" -gt 10
COPY --chown=jenkins:jenkins casc.yaml /var/jenkins_home/casc.yaml
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
