/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mykafka.Bus;
import mykafka.BusReader;
import mykafka.Card;
import mykafka.Letter;
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
                        if(exists){
                            
                            try{
                                MongoClient mongoClient = MongoAPI.connect();
                                DB db = mongoClient.getDB("BeAware");
                                DBCollection collection = db.getCollection(collectionName);
                                
                                DBCursor cursor = collection.find();
                                //ArrayList<TwitterReportLine> twitterReportLines = new ArrayList<>();
                                while (cursor.hasNext()) {
                                    DBObject post = cursor.next();
                                    boolean relevancy = (boolean) post.get("estimated_relevancy");

                                    if(relevancy){
                                        long timestamp_ms = System.currentTimeMillis();
                                        String id = post.get("id_str").toString();
                                        String date = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").format(new java.util.Date (timestamp_ms));
                                        BasicDBObject change = new BasicDBObject();
                                        change.append("$set", new BasicDBObject().append("created_at_live", date));
                                        BasicDBObject query = new BasicDBObject().append("id_str", id);
                                        collection.update(query, change);

                                        Letter letter = new Letter();
                                        letter.addTweetID(id);
                                        letter.setTimestamp(timestamp_ms);
                                        letter.setCollection(collectionName);
                                        String message = gson.toJson(letter);
                                        
                                        //DBObject user = (BasicDBObject) post.get("user");
                                        //twitterReportLines.add(new TwitterReportLine(post.get("text").toString(),user.get("name").toString(),post.get("created_at").toString(),id));

                                        try{
                                            bus.post(Configuration.socialMediaText001, message);
                                        }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                                            System.out.println("Error on send: " + e);
                                        }
                            
                                        TimeUnit.SECONDS.sleep(3);
                                    }
                                }
                                
                                //String twitterReport = TwitterReport.generateReport(twitterReportLines);
                                String twitterReport = TwitterReport.getDummyMessage(collectionName);

                                try{
                                    bus.post(Configuration.incidentTopic021, twitterReport);
                                }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                                    System.out.println("Error on send: " + e);
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
}
