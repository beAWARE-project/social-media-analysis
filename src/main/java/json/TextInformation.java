/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

/**
 *
 * @author andreadisst
 */
public class TextInformation {
    
    String ID;
    String type;
    String text;
    
    public TextInformation(){
        
    }
    
    public TextInformation(String ID, String type, String text){
        this.ID = ID;
        this.type = type;
        this.text = text;
    }
    
}
