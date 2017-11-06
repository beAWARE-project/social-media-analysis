/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import crawler.Configuration;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author andreadisst
 */
public class GetDistinctFields {
    
    public static void main(String[] args) throws UnknownHostException{
        
        Set<String> fields = new TreeSet<>();
        
        List<String> databases = new ArrayList<>();
        for(String language: Configuration.languages){
            for(String pilot: Configuration.pilots){
                databases.add("BeAware" + language + pilot);
            }
        }
        
        for(String database: databases){
            
            MongoCredential credential = MongoCredential.createCredential(Configuration.old_username, database, Configuration.old_password.toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(Configuration.old_host, Configuration.port), Arrays.asList(credential));
            DB db = mongoClient.getDB(database);

            for(String collection : Configuration.old_collections){
                DBCollection dbCollection = db.getCollection(collection);
                if(dbCollection.getCount() > 0){
                    DBCursor cursor = dbCollection.find();
                    for (String key: cursor.next().keySet()) {
                        fields.add(collection + "." + key);
                    }
                }
            }
            
        }
        
        for(String field : fields){
            System.out.println(field);
        }
        
    }
    
}
