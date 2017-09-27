FROM openjdk:7

COPY . /usr/src/social-media-analysis

WORKDIR /usr/src/social-media-analysis

RUN javac DemoCrawler.java

CMD ["java", "DemoCrawler"]