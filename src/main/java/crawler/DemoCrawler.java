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
    
    private static String useCase = "EnglishFloodsLive";
    private static String realCase = "EnglishFloods";
    private static Bus bus = new Bus();
    private static Gson gson = new Gson();
    
    public static void main(String[] args) throws InterruptedException {
        
        try{
        
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection(useCase);

            while( true ){
                
                DBCursor cursor = collection.find();
                if(cursor.size()==17){
                    
                    Letter letter = new Letter();
                    
                    while (cursor.hasNext()) {
                        DBObject post = cursor.next();
                        String id = post.get("id_str").toString();
                        if((boolean) post.get("estimated_relevancy")){
                            letter.addTweetID(id);
                        }
                    }

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    letter.setTimestamp(timestamp.getTime());
                    letter.setCollection(realCase);
                    String message = gson.toJson(letter);

                    try{
                        bus.post(Configuration.socialMediaTextDemo, message);
                    }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                        System.out.println("Error on send: " + e);
                    }
                    
                    //empty EnglishFloodsLive
                    cursor = collection.find();
                    while (cursor.hasNext()) {
                        collection.remove(cursor.next());
                    }

                    String report = "{\n" +
                    "    \"header\": {\n" +
                    "        \"topicName\": \"TOP101_INCIDENT_REPORT\",\n" +
                    "        \"topicMajorVersion\": 1,\n" +
                    "        \"topicMinorVersion\": 0,\n" +
                    "        \"sender\": \"SMA\",\n" +
                    "        \"msgIdentifier\": 554133,\n" +
                    "        \"sentUTC\": \"2018-01-01T12:00:00Z\",\n" +
                    "        \"status\": \"Actual\",\n" +
                    "        \"actionType\": \"Update\",\n" +
                    "        \"specificSender\": \"\",\n" +
                    "        \"scope\": \"\",\n" +
                    "        \"district\": \"Thessaloniki\",\n" +
                    "        \"recipients\": \"\",\n" +
                    "        \"code\": 0,\n" +
                    "        \"note\": \"\",\n" +
                    "        \"references\": \"\"\n" +
                    "    },\n" +
                    "    \"body\": {\n" +
                    "        \"incidentOriginator\": \"SMA\",\n" +
                    "        \"incidentID\": 432115,\n" +
                    "        \"language\": \"en-US\",\n" +
                    "        \"incidentCategory\": \"Other\",\n" +
                    "        \"incidentType\": \"Other\",\n" +
                    "        \"priority\": \"undefined\",\n" +
                    "        \"severity\": \"Unknown\",\n" +
                    "        \"certainty\": \"Observed\",\n" +
                    "        \"startTimeUTC\": \"2018-01-01T12:00:00Z\",\n" +
                    "        \"expirationTimeUTC\": \"2018-01-01T12:00:00Z\",\n" +
                    "        \"title\": \"\",\n" +
                    "        \"description\": \"flooded river flooding rain bridge\",\n" +
                    "        \"position\": {\n" +
                    "            \"latitude\": 45.43417,\n" +
                    "            \"longitude\": 12.33847\n" +
                    "        },\n" +
                    "        \"attachments\": [{\n" +
                    "                \"attachmentName\": \"TwitterReport3154.html\",\n" +
                    "                \"attachmentType\": \"webpage\",\n" +
                    "                \"attachmentTimeStampUTC\": \"2018-01-01T12:00:00Z\",\n" +
                    "                \"attachmentURL\": \"http://object-store-app.eu-gb.mybluemix.net/objectStorage?file=TwitterReport3154.html\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }\n" +
                    "}";

                    try{
                        bus.post(Configuration.incidentTopic101, report);
                    }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                        System.out.println("Error on send: " + e);
                    }
                    
                    TimeUnit.SECONDS.sleep(10);
                    
                }else{
                    TimeUnit.SECONDS.sleep(10);
                }
                
            }

            //bus.close();
            
        }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
            System.out.println("Error on demo crawler: " + e);
        }
        
    }
}
