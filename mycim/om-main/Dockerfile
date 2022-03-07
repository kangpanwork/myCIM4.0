FROM 118.123.246.35:47881/public/jdk8_x64:FA
MAINTAINER Eric qiheng.zhang@fa-software.com
ENV JAVA_OPTS="-Xmx2g"
#ENV LANG zh_CN.utf8
#ENV LANGUAGE zh_CN.utf8
#ENV LC_ALL zh_CN.utf8
#ENV TZ Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone


ENV TZ Asia/Shanghai

#ENV LANG en_US.utf8

WORKDIR /opt
COPY ./target/mycim*.jar /opt/app.jar
EXPOSE 8778 8208
ENTRYPOINT java $JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -Djava.net.preferIPv4Stack=true -Djava.security.egd=file:/dev/./urandom -jar /opt/app.jar