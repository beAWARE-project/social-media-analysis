/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

/**
 *
 * @author andreadisst
 */
public class TextResponse {
    
    String concepts = "";
    boolean relevancy = false;
    
    public TextResponse(){}
    
    public TextResponse(String concepts, boolean relevancy){
        this.concepts = concepts;
        this.relevancy = relevancy;
    }
    
    public String getConcepts(){
        return concepts;
    }
    
    public boolean getRelevancy(){
        return relevancy;
    }
    
}
