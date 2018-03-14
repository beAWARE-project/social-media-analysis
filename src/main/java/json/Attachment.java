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
public class Attachment {
    
    String attachmentID;
    String attachmentType;
    String attachmentText;
    String attachmentTimeStampUTC;
    
    public Attachment(){
        
    }
    
    public Attachment(String attachmentID, String attachmentType,
                        String attachmentText, String attachmentTimeStampUTC){
        this.attachmentID = attachmentID;
        this.attachmentType = attachmentType;
        this.attachmentText = attachmentText;
        this.attachmentTimeStampUTC = attachmentTimeStampUTC;
    }
    
}
