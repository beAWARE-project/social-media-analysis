FROM openjdk:7

COPY . /usr/src/social-media-analysis

WORKDIR /usr/src/social-media-analysis

RUN javac -classpath social-media-analysis.jar src/main/java/crawler/DemoCrawler.java

CMD ["java", "DemoCrawler"]