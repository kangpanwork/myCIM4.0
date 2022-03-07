FROM openjdk:8u212-jre
MAINTAINER Eric qiheng.zhang@fa-software.com
ENV JAVA_OPTS=""
#ENV LANG zh_CN.utf8
#ENV LANGUAGE zh_CN.utf8
#ENV LC_ALL zh_CN.utf8
ENV TZ Asia/Shanghai
WORKDIR /opt
COPY ./target/sentinel-history*.jar /opt/app.jar
EXPOSE 8785 8210
ENTRYPOINT java $JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -jar /opt/app.jar