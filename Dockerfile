FROM openjdk:8

COPY . /usr/src/social-media-analysis

WORKDIR /usr/src/social-media-analysis

CMD ["java", "-jar", "target/social-media-analysis.jar"]
