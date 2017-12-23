FROM java:8u111-jdk

ENV ARTIFACTID mavendockerplugindemo
ENV ARTIFACTVERSION 0.0.2-SNAPSHOT
ENV HOME_PATH /home

ADD /$ARTIFACTID-$ARTIFACTVERSION.jar $HOME_PATH/mavendockerplugindemo.jar

WORKDIR $HOME_PATH

ENTRYPOINT ["java","-jar","mavendockerplugindemo.jar"]
