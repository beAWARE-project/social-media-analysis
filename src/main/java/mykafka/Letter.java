/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mykafka;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andreadisst
 */
public class Letter {
    
    List<String> tweetIDs = new ArrayList<>();
    long timestamp;
    String collection;
    
    public Letter(){
        
    }
    
    public Letter(List<String> tweetIDs, long timestamp, String collection){
        this.tweetIDs = tweetIDs;
        this.timestamp = timestamp;
        this.collection = collection;
    }
    
    public void addTweetID(String tweetID){
        tweetIDs.add(tweetID);
    }
    
    public void setTweetIDsList(List<String> tweetIDs){
        this.tweetIDs = tweetIDs;
    }
    
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    
    public void setCollection(String collection){
        this.collection = collection;
    }
    
}
