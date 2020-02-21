FROM adoptopenjdk/openjdk11
VOLUME /tmp
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/share/revolut/myservice.jar
ENTRYPOINT ["java","-jar", "/usr/share/revolut/myservice.jar"]
