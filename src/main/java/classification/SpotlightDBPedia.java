/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import crawler.Configuration;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author andreadisst
 */
public class SpotlightDBPedia {
    
    public static void main(String[] args) throws UnknownHostException{
        
        List<String> useCases = new ArrayList<>();
        for(String language: Configuration.languages){
            for(String pilot: Configuration.pilots){
                useCases.add(language + pilot);
            }
        }
        
        MongoClient mongoClient = new MongoClient(Configuration.host, Configuration.port);
        DB db = mongoClient.getDB(Configuration.database);
        db.authenticate(Configuration.username, Configuration.password.toCharArray());
        
        for(String useCase: useCases){
            System.out.println(useCase);
            DBCollection collection = db.getCollection(useCase);

            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("relevant", false);
            whereQuery.put("concepts", new BasicDBObject("$exists", false));
            DBCursor cursor = collection.find(whereQuery);
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                String id = obj.get("id_str").toString();
                String text = obj.get("text").toString();
                String concepts = getConceptsFromSpotlightDBPedia(text,useCase);
               
                BasicDBObject newDocument = new BasicDBObject();
                newDocument.append("$set", new BasicDBObject().append("concepts", concepts));
                BasicDBObject searchQuery = new BasicDBObject().append("id_str", id);
                collection.update(searchQuery, newDocument);
            }
        }
    }
    
    public static String getConceptsFromSpotlightDBPedia(String text, String useCase){
        String concepts = "";
        
        if(!useCase.contains("Greek") && !useCase.contains("Danish")){ //these languages are not supported by Spotlight
            
            String language_prefix = "en";
            String concept_prefix = "http://dbpedia.org/resource/";

            if(useCase.contains("Spanish")){
                language_prefix = "es";
                concept_prefix = "http://es.dbpedia.org/resource/";
            }else if(useCase.contains("Italian")){
                language_prefix = "it";
                concept_prefix = "http://it.dbpedia.org/resource/";
            }

            String url = "http://model.dbpedia-spotlight.org/" + language_prefix + "/annotate";
            HashMap<String,String> parameters = new HashMap<>();
            parameters.put("text",text);
            parameters.put("confidence","0.20");

            try{
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("content-type","application/x-www-form-urlencoded");

                String urlParameters = "";
                String prefix = "";
                for (Map.Entry<String, String> entry : parameters.entrySet())
                {
                    String key = entry.getKey();
                    String value = URLEncoder.encode(entry.getValue() , "utf-8");
                    urlParameters+= prefix + key + "=" + value;
                    prefix = "&";
                }

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();

                if(responseCode==200){
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                    String response = in.readLine();
                    in.close();
                    response = response.replace("@", "");

                    Gson g = new Gson(); 
                    SpotlightResponse sr = g.fromJson(response, SpotlightResponse.class);
                    if(sr.getResources()!=null){
                        List<Resource> resources = sr.getResources();
                        for(Resource resource : resources){
                            concepts += resource.getURI().replace(concept_prefix,"") + " ";
                        }
                        concepts = concepts.replace("Hypertext_Transfer_Protocol", "").replace("HTTP_Secure", "").replace("Hypertext_Transfer_Protocol_Secure","").trim().replaceAll(" +", " ");
                    }
                }
            }
            catch(IOException io){
                System.out.println("Error in SpotlightDBPedia: " + io.getMessage());
            }
        }
        
        return concepts;
    }
    
}
