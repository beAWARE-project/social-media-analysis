/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mykafka;

/**
 *
 * @author andreadisst
 */
public class Card {
    
    String collection;
    
    public Card(){
        
    }
    
    public Card(String collection){
        this.collection = collection;
    }
    
    public String getCollection(){
        return collection;
    }
    
}
