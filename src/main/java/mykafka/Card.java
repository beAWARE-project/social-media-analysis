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
    int bunch;
    
    public Card(){
        
    }
    
    public Card(String collection, int bunch){
        this.collection = collection;
        this.bunch = bunch;
    }
    
    public String getCollection(){
        return collection;
    }
    
    public int getBunch(){
        return bunch;
    }
    
}
