FROM openjdk:8

COPY resources /usr/src/social-media-analysis/resources

COPY target/social-media-analysis-1.1-fat.jar /usr/src/social-media-analysis

WORKDIR /usr/src/social-media-analysis

CMD ["java", "-jar", "social-media-analysis-1.1-fat.jar"]
