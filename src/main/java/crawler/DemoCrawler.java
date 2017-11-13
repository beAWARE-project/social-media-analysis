/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
    private static Bus bus = new Bus();
    private static Gson gson = new Gson();
    
    public static void main(String[] args) throws InterruptedException {
        
        try{
        
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection(useCase);

            while( true ){
                
                DBCursor cursor = collection.find();
                while (cursor.hasNext()) {
                    
                    DBObject post = cursor.next();
                    String id = post.get("id_str").toString();

                    //insert(post.toString(), useCase); 

                    Letter letter = new Letter();
                    letter.addTweetID(id);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    letter.setTimestamp(timestamp.getTime());
                    letter.setCollection(useCase);
                    String message = gson.toJson(letter);
                    System.out.println(message);
                    try{
                        bus.post(Configuration.socialMediaTextDemo, message);
                    }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                        System.out.println("Error on send: " + e);
                    }
                    
                    TimeUnit.SECONDS.sleep(10);
                }
                
                TimeUnit.MINUTES.sleep(1);
            }

            //bus.close();
            
        }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
            System.out.println("Error on demo crawler: " + e);
        }
        
    }
}
