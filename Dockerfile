FROM openjdk:10-jdk-slim
MAINTAINER Natel Energy https://natelenergy.com/

WORKDIR /var/excel

ADD target/excel-server-1.0-SNAPSHOT.jar /var/excel/excel-server.jar
ADD config.yml /var/excel/config.yml

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "--add-modules", "java.xml.bind", "-jar", "excel-server.jar", "server", "config.yml"]
