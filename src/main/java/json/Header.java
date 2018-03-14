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
public class Header {
    
    String topicName;
    int topicMajorVersion;
    int topicMinorVersion;
    String sender;
    String msgIdentifier;
    String sentUTC;
    String status;
    String actionType;
    String specificSender;
    String scope;
    String district;
    String recipients;
    int code;
    String note;
    String references;
    
    public Header(){
        
    }
    
    public Header(String topicName, int topicMajorVersion, int topicMinorVersion,
                    String sender, String msgIdentifier, String sentUTC, String status,
                    String actionType, String specificSender, String scope,
                    String district, String recipients, int code, String note,
                    String references){
        this.topicName = topicName;
        this.topicMajorVersion = topicMajorVersion;
        this.topicMinorVersion = topicMinorVersion;
        this.sender = sender;
        this.msgIdentifier = msgIdentifier;
        this.sentUTC = sentUTC;
        this.status = status;
        this.actionType = actionType;
        this.specificSender = specificSender;
        this.scope = scope;
        this.district = district;
        this.recipients = recipients;
        this.code = code;
        this.note = note;
        this.references = references;
    }
    
}
