FROM openjdk:8
RUN apt-get update
RUN apt-get install -y maven
RUN mkdir -p /opt/simulation
WORKDIR /opt/simulation
ADD . /opt/simulation
RUN mvn install
CMD mvn clean package; java -jar ./activforms/target/activforms-0.0.1-SNAPSHOT-spring-boot.jar
