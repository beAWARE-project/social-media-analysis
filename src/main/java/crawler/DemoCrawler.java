/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import classification.Classification;
import classification.ImageResponse;
import classification.Validation;
import classification.VerificationResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import json.*;
import mykafka.Bus;
import mykafka.BusReader;
import mykafka.Card;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 *
 * @author andreadisst
 */
public class DemoCrawler {
    
    private static Bus bus = new Bus();
    private static Gson gson = new Gson();
    private static BusReader busReader = new BusReader();
    
    public static void main(String[] args) throws InterruptedException, IOException {
        
        KafkaConsumer<String, String> kafkaConsumer = busReader.getKafkaConsumer();
        kafkaConsumer.subscribe(Arrays.asList(Configuration.socialMediaTrigger));
        
        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                for (ConsumerRecord<String, String> record : records)
                {
                    String receivedMessage = record.value();
                    
                    Type type = new TypeToken<Card>() {}.getType();
                    
                    try{
                        Card card = gson.fromJson(receivedMessage, type);
                        String collectionName = card.getCollection();
                        boolean exists = false;
                        for(String language:Configuration.languages){
                            for(String pilot:Configuration.pilots){
                                if(collectionName.equals(language+pilot)){
                                    exists = true;
                                }
                            }
                        }
                        if(exists && card.getBunch() > 0 && card.getBunch() <6){
        
                            try{
                                MongoClient mongoClient = MongoAPI.connect();
                                DB db = mongoClient.getDB("BeAware");
                                DBCollection collection = db.getCollection(collectionName);
                                
                                BasicDBObject query = new BasicDBObject("bunch", card.getBunch());
                                DBCursor cursor = collection.find(query);
                                while (cursor.hasNext()) {
                                    DBObject post = cursor.next();
                                    
                                    save(post.toString(),collectionName);
                                    
                                    TimeUnit.SECONDS.sleep(3);
                                }
                                
                                mongoClient.close();
            
                            }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
                                System.out.println("Error on demo crawler: " + e);
                            }
                        }
                        
                    }catch(JsonSyntaxException e){
                        System.out.println(e);
                    }
                    }
            }
        } finally {
          kafkaConsumer.close(); 
        }
    }
        
    private static void save(String msg, String useCase) throws UnknownHostException, NoSuchAlgorithmException, KeyManagementException{
        
        JsonObject obj = new JsonParser().parse(msg).getAsJsonObject();
        
        String text = getText(obj);
        Position position = getLocation(text); //this could be added to json
        obj = updateText(obj);
        text = getText(obj);
        
        /* STEP ONE - Detect fake tweets */
        
        boolean isVerified = true;
        Double confidence_value = 0.0;
        
        JsonObject user = obj.get("user").getAsJsonObject();
        String user_id = user.get("id_str").getAsString();
        if(!user_id.equals("920984955047567360")){
            VerificationResponse verification = Validation.verifyTweet(obj.toString());
            isVerified = verification.getPredictedValue();
            confidence_value = verification.getConfidenceValue();
            System.out.println("-> verification : "+isVerified+" ");
        }
        JsonObject verificationObj = new JsonObject();
        verificationObj.addProperty("predicted", isVerified);
        verificationObj.addProperty("confidence", confidence_value);
        obj.add("verification", verificationObj);
        
        if(!isVerified){
            insert(obj, useCase);
        }else{
            /* STEP TWO - Check emoticons/emojis */

            boolean emoticon_relevancy = Validation.EmoticonsEstimation(text);
            System.out.print("-> emoticon classification : "+emoticon_relevancy+" ");
            if(!emoticon_relevancy){
                obj.addProperty("emoticon_relevancy", false);
                insert(obj, useCase);
            }else{
                obj.addProperty("emoticon_relevancy", true);

                /* STEP THREE - Classificy based on visual or textual information */

                boolean estimated_relevancy = false;
                String imageURL = getImageURL(obj);
                if(!imageURL.equals("")){
                    System.out.print("-> image classification ");
                    ImageResponse ir = Classification.classifyImage(imageURL, useCase);
                    estimated_relevancy = ir.getRelevancy();
                    System.out.print(": "+estimated_relevancy+" ");
                    obj.addProperty("dcnn_feature", ir.getDcnnFeature());
                }

                if(estimated_relevancy){
                    obj.addProperty("estimated_relevancy", true);
                    insert(obj, useCase);
                    forward(obj, useCase, position);
                }else if(!estimated_relevancy || imageURL.equals("")){
                    if(useCase.equals("ItalianFloods")||useCase.equals("GreekHeatwave")||useCase.equals("SpanishFires")){
                        System.out.print("-> text classification ");
                        String estimated_relevancy_str = Classification.classifyText(text, useCase);
                        if(estimated_relevancy_str.equals("")){
                            if(!imageURL.equals("")){ obj.addProperty("estimated_relevancy", false); }
                            insert(obj, useCase);
                        }else if(estimated_relevancy_str.equals("true")){
                            System.out.print(": "+estimated_relevancy_str+" ");
                            obj.addProperty("estimated_relevancy", true);
                            insert(obj, useCase);
                            forward(obj, useCase, position);
                        }else if(estimated_relevancy_str.equals("false")){
                            System.out.print(": "+estimated_relevancy_str+" ");
                            obj.addProperty("estimated_relevancy", false);
                            insert(obj, useCase);
                        }
                    }else{
                        if(!imageURL.equals("")){
                            obj.addProperty("estimated_relevancy", false);
                            insert(obj, useCase);
                        }else{
                            insert(obj, useCase);
                            forward(obj, useCase, position);
                        }
                    }
                }
            }
        }
    }
    
    private static String getText(JsonObject obj){
        String text = "";
        if(obj.has("extended_tweet")){
            JsonObject extended_tweet = obj.get("extended_tweet").getAsJsonObject();
            text = extended_tweet.get("full_text").getAsString();
        }else if(obj.get("text") != null){
            text = obj.get("text").getAsString();
        }
        return text;
    }
    
    private static String getImageURL(JsonObject obj){
        String imageURL = "";
        if(obj.has("extended_tweet")){
            JsonObject extended_tweet = obj.get("extended_tweet").getAsJsonObject();
            if(extended_tweet.has("entities")){
                JsonObject entities = extended_tweet.get("entities").getAsJsonObject();
                if(entities.has("media")){
                    JsonArray media = entities.get("media").getAsJsonArray();
                    if(media.size() > 0){
                        JsonObject image = media.get(0).getAsJsonObject();
                        if(image.has("media_url")){
                            imageURL = image.get("media_url").getAsString();
                        }
                    }
                }
            }
        }
        else if(obj.has("entities")){
            JsonObject entities = obj.get("entities").getAsJsonObject();
            if(entities.has("media")){
                JsonArray media = entities.get("media").getAsJsonArray();
                if(media.size() > 0){
                    JsonObject image = media.get(0).getAsJsonObject();
                    if(image.has("media_url")){
                        imageURL = image.get("media_url").getAsString();
                    }
                }
            }
        }
        return imageURL;
    }
    
    private static JsonObject updateText(JsonObject obj){
        if(obj.has("extended_tweet")){
            JsonObject extended_tweet = obj.get("extended_tweet").getAsJsonObject();
            String text = extended_tweet.get("full_text").getAsString();
            text = cleanText(text);
            text = replaceLocation(text);
            obj.getAsJsonObject("extended_tweet").addProperty("full_text", text);
        }else if(obj.get("text") != null){
            String text = obj.get("text").getAsString();
            text = cleanText(text);
            text = replaceLocation(text);
            obj.addProperty("text", text);
        }
        return obj;
    }
    
    private static void insert(JsonObject obj, String useCase){
        
        obj.remove("_id");
        
        long now = System.currentTimeMillis();
        String mongoDate = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").format(new java.util.Date (now));
        obj.addProperty("created_at", mongoDate);
        obj.addProperty("timestamp_ms", String.valueOf(now));
        
        try {
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection("Consumer");
            BasicDBObject res = (BasicDBObject) JSON.parse(obj.toString());

            collection.insert(res);
            System.out.print("-> saved\n");
            
            mongoClient.close();
        } catch (UnknownHostException | NoSuchAlgorithmException | KeyManagementException ex) {
            
        }
        
    }
    
    private static void forward(JsonObject obj, String useCase, Position position){
        String id = obj.get("id_str").getAsString();

        String language = "";
        if(useCase.contains("English")){
            language = "en-US";
        }else if(useCase.contains("Italian")){
            language = "it-IT";
        }else if(useCase.contains("Greek")){
            language = "el-GR";
        }else if(useCase.contains("Spanish")){
            language = "es-ES";
        }
        
        String district = "";
        if(useCase.contains("Floods")){
            district = "Vicenza";
        }else if(useCase.contains("Heatwave")){
            district = "Thessaloniki";
        }else if(useCase.contains("Fires")){
            district = "Valencia";
        }

        long now = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date(now));

        Header header = new Header(Configuration.socialMediaText001, 0, 1, "SMA", "sma-msg-"+now, date, "Actual", "Alert", "citizen", "Restricted", district, "", 0, "", "");

        Body body;
        if(position.getLatitude()==0.0 && position.getLongitude()==0.0){
            body = new Body("SMA", "INC_SMA_"/*+useCase+"_"*/+id, language, date, getText(obj));
        }else{
            body = new Body("SMA", "INC_SMA_"/*+useCase+"_"*/+id, language, date, getText(obj), position);
        }

        Message message = new Message(header, body);

        String message_str = gson.toJson(message);
        try{
            bus.post(Configuration.socialMediaText001, message_str);
        }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
            System.out.println("Error on send: " + e);
        }
    }
    
    private static Position getLocation(String msg){
        Position position = new Position(0,0); //default Thessaloniki?
        
        if(msg.contains("S32ap")){
            return new Position(45.5493, 11.5497);
        }else if(msg.contains("M90xz")){
            return new Position(45.5502, 11.5505);
        }else if(msg.contains("3vg87")){
            return new Position(45.5505, 11.5450);
        }else if(msg.contains("F77ad")){
            return new Position(45.5522, 11.5494);
        }else if(msg.contains("C44ud")){
            return new Position(45.5455, 11.5354);
        }
        
        /*if(msg.contains("ΚΘ_4")){
            return new Position(40.6207, 22.9649);
        }else if(msg.contains("ΚΘ_6")){
            return new Position(40.6019, 22.9736);
        }else if(msg.contains("ΠΑΤ")){
            return new Position(40.6325, 22.9407);
        }else if(msg.contains("ΠΧ")){
            return new Position(40.6008, 22.9701);
        }else if(msg.contains("ΠΤ")){
            return new Position(40.6140, 22.9722);
        }else if(msg.contains("ΔΕ")){
            return new Position(40.6333, 22.9495);
        }else if(msg.contains("ΔΤ")){
            return new Position(40.6266, 22.9526);
        }else if(msg.contains("ΔΒ")){
            return new Position(40.5956, 22.9600);
        }*/
        
        return position;
    }
    
    private static String replaceLocation(String msg){
        String tweet = msg;
        
        tweet = tweet.replace("S32ap", "Matteotti").replace("M90xz", "Angeli").replace("C44ud", "Vicenza").replace("F77ad", "Bacchiglione").replace("3vg87","Pusterla");
        
        /*tweet = tweet.replace("ΚΘ_4", "4ο ΚΑΠΗ").replace("ΚΘ_6", "6ο ΚΑΠΗ").replace("ΠΑΤ", "Πλατεία Αριστοτέλους").replace("ΠΧ", "Χαριλάου").replace("ΠΤ", "Τούμπα")
                .replace("ΔΕ", "Εγνατία").replace("ΔΤ", "Τσιμισκή").replace("ΔΒ", "Βούλγαρη");*/
        
        return tweet;
    }
    
    private static String cleanText(String msg){
        String tweet = msg;
        String pattern = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
        
        tweet = tweet.replace("#THIS_IS_A_TEST", "").replace("#beawaretest", "");
        
        tweet = tweet.replaceAll(pattern, " @user");
        
        return tweet;
    }
}
