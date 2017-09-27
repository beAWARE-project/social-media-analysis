/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import crawler.Configuration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author andreadisst
 */
public class Classification {
    
    private static final double EPSILON = 0.3;
    private static final int MIN_TRAIN_DATA = 20;
    
    public static ImageResponse classifyImage(String imageURL, String useCase){
        
        ImageResponse ir = new ImageResponse();
        
        if(!useCase.contains("Fires") && !useCase.contains("Heatwave")){ //currently not supported
            
            String concept = "";
            if(useCase.contains("Fires")){
                concept = "fires";
            }else if(useCase.contains("Heatwave")){
                concept = "heatwave";
            }else if(useCase.contains("Floods")){
                concept = "flood";
            }
            
            try {

                URL url = new URL("http://160.40.49.111:911/api/floodDetectionService/query?imageURL="+imageURL+"&concept="+concept);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    try{
                        String output = br.readLine();
                        if(!output.equals("null")){
                            JsonObject obj = new JsonParser().parse(output).getAsJsonObject();
                            ir = new ImageResponse(obj.get("dcnnFeature").getAsString().replace("\"", ""), Boolean.parseBoolean(obj.get("relevancy").getAsString()));
                        }
                    } catch (JsonSyntaxException | IOException e){
                        System.out.println("Error on image classification: " + e);
                    }
                }

                conn.disconnect();

            } catch (MalformedURLException e) {
                System.out.println("Error on image classification: " + e);
            } catch (IOException e) {
                System.out.println("Error on image classification: " + e);
            }
            
        }
        
        return ir;
    }
    
    public static TextResponse classifyText(String text, String useCase, DB db){
        
        String concepts = SpotlightDBPedia.getConceptsFromSpotlightDBPedia(text,useCase);
        TextResponse tr = new TextResponse(concepts, false);
        
        if(!concepts.equals("")){
            String[] conceptList = concepts.split(" ");
            ArrayList<Double> similarities = new ArrayList<>();
            
            DBCollection collection = db.getCollection(useCase);

            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("relevant", true);
            DBCursor cursor = collection.find(whereQuery);
            if(cursor.size() > MIN_TRAIN_DATA){
                while (cursor.hasNext()) {
                    DBObject obj = cursor.next();
                    if(obj.containsField("concepts")){
                        similarities.add(Jaccard.getJaccardSimilarity(conceptList, obj.get("concepts").toString().split(" ")));
                    }
                }
                if(!similarities.isEmpty()){
                    double max = Collections.max(similarities);
                    if(max > EPSILON){
                        tr = new TextResponse(concepts, true);
                    }
                }
            }
        }
        return tr;
    }
    
}
