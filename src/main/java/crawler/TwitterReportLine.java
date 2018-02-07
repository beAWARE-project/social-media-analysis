/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

/**
 *
 * @author andreadisst
 */
public class TwitterReportLine {
    
    private String text, user, date, id;
    
    public TwitterReportLine(String text, String user, String date, String id){
        this.text = text;
        this.user = user;
        this.date = date;
        this.id = id;
    }
    
    public String getText(){
        return text;
    }
    
    public String getUser(){
        return user;
    }
    
    public String getDate(){
        return date;
    }
    
    public String getId(){
        return id;
    }
    
}
