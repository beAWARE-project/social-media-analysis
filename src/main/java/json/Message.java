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
public class Message {
    
    Header header;
    Body body;
    
    public Message(){
        
    }
    
    public Message(Header header, Body body){
        this.header = header;
        this.body = body;
    }
    
    public Body getBody(){
        return body;
    }
    
    public Header getHeader(){
        return header;
    }
    
}
