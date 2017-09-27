/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import classification.Classification;
import classification.ImageResponse;
import classification.TextResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import mykafka.Bus;
import mykafka.Letter;

/**
 *
 * @author andreadisst
 */
public class TweetsCrawler {
    
    private static Map<String, List<String>> keywordsPerCollection = new HashMap<>();
    private static List<String> useCases = new ArrayList<>();
    private static DB db;
    private static Bus bus = new Bus();
    private static Gson gson = new Gson();
    private static Client hosebirdClient;
    private static BlockingQueue<String> msgQueue;
    
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        
        for(String language: Configuration.languages){
            for(String pilot: Configuration.pilots){
                useCases.add(language + pilot);
            }
        }
        
        prepareStreamingAPI();
        
        while (!hosebirdClient.isDone()) {
            String msg = msgQueue.take();
            findDatabaseAndInsert(msg);
        }
        
        bus.close();
        
    }
    
    private static void prepareStreamingAPI() throws UnknownHostException{
        
        MongoClient mongoClient = new MongoClient( Configuration.host , Configuration.port);
        db = mongoClient.getDB(Configuration.database);
        db.authenticate(Configuration.username, Configuration.password.toCharArray());
        
        List<String> keywords = getKeywords();
        
        msgQueue = new LinkedBlockingQueue<String>(16); //10,000
        //BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        hosebirdEndpoint.trackTerms(keywords);

        Authentication hosebirdAuth = new OAuth1(Configuration.consumerKey, Configuration.consumerSecret, Configuration.token, Configuration.secret);
        
        
        ClientBuilder builder = new ClientBuilder()
            .name("Hosebird-Client-01")                              // optional: mainly for the logs
            .hosts(hosebirdHosts)
            .authentication(hosebirdAuth)
            .endpoint(hosebirdEndpoint)
            .processor(new StringDelimitedProcessor(msgQueue));
            //.eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        hosebirdClient = builder.build();
        hosebirdClient.connect();
        
    }
    
    private static List<String> getKeywords() throws UnknownHostException{
        List<String> allKeywords = new ArrayList<>();
        
        DBCollection collection = db.getCollection("Feeds");

        DBCursor cursor = collection.find();

        while (cursor.hasNext()) {
            DBObject post = cursor.next();
            List<String> keywords = (List<String>) post.get("keywords");
            String useCase = post.get("label").toString().replace("BeAware", "");
            keywordsPerCollection.put(useCase, keywords);
            for(String keyword : keywords){
                if(!allKeywords.contains(keyword)){
                    allKeywords.add(keyword);
                }
            }
        }
        
        return allKeywords;
    }
    
    private static void findDatabaseAndInsert(String msg) throws UnknownHostException{
        JsonObject obj = new JsonParser().parse(msg).getAsJsonObject();
        if(obj.get("text") != null){
            String text = obj.get("text").toString();
            for(String useCase : useCases){
                List<String> keywords = keywordsPerCollection.get(useCase);
                for(String keyword : keywords){
                    if(text.contains(keyword)){
                        try{
                            System.out.print("Insert tweet to " + useCase);
                            insert(msg, useCase);
                        }catch(Exception e){
                            System.out.println("Error: " + e);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private static void insert(String msg, String useCase) throws UnknownHostException{
        
        boolean relevancy = false;
        
        JsonObject obj = new JsonParser().parse(msg).getAsJsonObject();
        
        if(obj.has("entities")){
            JsonObject entities = obj.get("entities").getAsJsonObject();
            if(entities.has("media")){
                JsonArray media = entities.get("media").getAsJsonArray();
                if(media.size() > 0){
                    JsonObject image = media.get(0).getAsJsonObject();
                    if(image.has("media_url")){
                        System.out.print(" -> image classification");
                        String imageURL = image.get("media_url").getAsString();
                        ImageResponse ir = Classification.classifyImage(imageURL, useCase);
                        relevancy = ir.getRelevancy();
                        
                        image.addProperty("dcnn_feature", ir.getDcnnFeature());
                        media.set(0,image);
                        entities.add("media", media);
                        obj.add("entities", entities);
                    }
                }
            }
        }
        
        if(!relevancy){
            System.out.print(" -> text classification");
            TextResponse tr = Classification.classifyText(obj.get("text").getAsString(), useCase, db);
            obj.addProperty("concepts", tr.getConcepts());
            relevancy = tr.getRelevancy();
        }
        
        System.out.println(" -> " + relevancy);
        obj.addProperty("estimated_relevancy", relevancy);
        
        DBCollection collection = db.getCollection(useCase);
        BasicDBObject res = (BasicDBObject) JSON.parse(obj.getAsString());
        collection.insert(res);
        
        if(relevancy){
            Letter letter = new Letter();
            letter.addTweetID(res.get("id_str").toString());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            letter.setTimestamp(timestamp.getTime());
            letter.setCollection(useCase);

            String message = gson.toJson(letter);
            try{
                bus.post(Configuration.socialMediaText, message);
            }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                System.out.println("Error on send: " + e);
            }
        }
    }
    
}
