/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    //private static CDR cdr = new CDR();
    private static Gson gson = new Gson();
    
    public static void main(String[] args) throws InterruptedException, IOException {
        
        try{
        
            MongoClient mongoClient = MongoAPI.connect();
            DB db = mongoClient.getDB("BeAware");
            DBCollection collection = db.getCollection(useCase);
            DBCollection realCollection = db.getCollection(realCase);

            while( true ){
                
                DBCursor cursor = collection.find();
                if(cursor.size()==17){
                    
                    //ArrayList<ReportPiece> reportPieces = new ArrayList<>();
                    
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
                            realCollection.update(query, change);
                            
                            Letter letter = new Letter();
                            letter.addTweetID(id);
                            letter.setTimestamp(timestamp_ms);
                            letter.setCollection(realCase);
                            String message = gson.toJson(letter);
                            
                            //DBObject user = (BasicDBObject) post.get("user");
                            //reportPieces.add(new ReportPiece(post.get("text").toString(),user.get("name").toString(),post.get("created_at").toString(),id));
                            
                            try{
                                bus.post(Configuration.socialMediaTextDemo, message);
                            }catch(IOException | InterruptedException | ExecutionException | TimeoutException e){
                                System.out.println("Error on send: " + e);
                            }
                            
                            TimeUnit.SECONDS.sleep(3);
                        }
                        
                    }
                    
                    //String twitterReport = generateReport(reportPieces);
                    String twitterReport = "TwitterReport1516098723965.html";
                    
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
                    "        \"msgIdentifier\": \"SMA00000001\",\n" +
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
                    "        \"incidentID\": \"432115\",\n" +
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
                    "                \"attachmentName\": \""+twitterReport+"\",\n" +
                    "                \"attachmentType\": \"webpage\",\n" +
                    "                \"attachmentTimeStampUTC\": \"2018-01-01T12:00:00Z\",\n" +
                    "                \"attachmentURL\": \"http://object-store-app.eu-gb.mybluemix.net/objectStorage?file="+twitterReport+"\"\n" +
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
                    TimeUnit.SECONDS.sleep(5);
                }
                
            }

            //bus.close();
            
        }catch(UnknownHostException | KeyManagementException | NoSuchAlgorithmException e){
            System.out.println("Error on demo crawler: " + e);
        }
        
    }
    
    /*private static String generateReport(ArrayList<ReportPiece> reportPieces) throws IOException{
        
        String filename = "TwitterReport"+System.currentTimeMillis()+".html";
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        
        for(ReportPiece reportPiece : reportPieces){
            String line = "<div><p><b>"+reportPiece.getText()+"</b></p><p><i>posted by</i> "+
                    reportPiece.getUser()+" <i>at</i> "+reportPiece.getDate()+"</p><p><a href=\"https://twitter.com/statuses/"+
                    reportPiece.getId()+"\">View on Twitter</a></div><br>";
            writer.write(line);
        }
        
        writer.close();
        
        cdr.storeFile(filename, filename);
        
        File file = new File(filename);
        file.delete();
        
        return filename;
    }*/
}
