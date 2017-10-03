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
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mykafka.Bus;
import mykafka.Letter;

/**
 *
 * @author andreadisst
 */
public class DemoCrawler {
    
    private static String useCase = "EnglishFloods";
    private static DB db;
    private static Bus bus = new Bus();
    private static Gson gson = new Gson();
    private static int limit = 2240506;
    
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        
        while(true){
            Letter letter = new Letter();
            letter.addTweetID("1001003");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            letter.setTimestamp(timestamp.getTime());
            letter.setCollection(useCase);
            String message = gson.toJson(letter);
            try{
                bus.post(Configuration.socialMediaTextDemo, message);
            }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                System.out.println("Error on send: " + e);
            }
            TimeUnit.SECONDS.sleep(3);
        }
        
        //After connection with mongo is restored
        /*MongoClient mongoClient = new MongoClient(Configuration.local_host, Configuration.port);
        db = mongoClient.getDB(Configuration.database);
        db.authenticate(Configuration.username, Configuration.password.toCharArray());
        
        DBCollection collection = db.getCollection(useCase);
        
        int skip = 0;
        while( skip < limit ){
            DBCursor cursor = collection.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT).skip(skip).limit(1);
            DBObject post = cursor.next();
            String id = post.get("id_str").toString();
            
            //insert(post.toString(), useCase); 
            
            Letter letter = new Letter();
            letter.addTweetID(id);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            letter.setTimestamp(timestamp.getTime());
            letter.setCollection(useCase);
            String message = gson.toJson(letter);
            try{
                bus.post(Configuration.socialMediaTextDemo, message);
            }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                System.out.println("Error on send: " + e);
            }
            
            skip++;
            if(skip==limit)
                skip = 0;
        }
        
        bus.close();*/
        
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
                        ImageResponse ir = Classification.classifyImage(imageURL, useCase); //change IP
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
        collection.insert(res); //update with relevancy
        
        if(relevancy){
            Letter letter = new Letter();
            letter.addTweetID(res.get("id_str").toString());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            letter.setTimestamp(timestamp.getTime());
            letter.setCollection(useCase);

            String message = gson.toJson(letter);
            try{
                bus.post(Configuration.socialMediaTextDemo, message);
            }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                System.out.println("Error on send: " + e);
            }
        }
    }
    
}
