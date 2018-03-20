/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andreadisst
 */
public class Body {
    
    String incidentOriginator;
    String incidentID;
    String language;
    /*String incidentCategory;
    String incidentType;
    String priority;
    String severity;
    String certainty;
    String expirationTimeUTC;
    String title;*/
    String startTimeUTC;
    String description;
    Position position;
    
    public Body(){
        
    }
    
    public Body(String incidentOriginator, String incidentID, String language,
                    /*String incidentCategory, String incidentType, String priority, String severity,
                    String certainty, String expirationTimeUTC, String title,*/ String startTimeUTC, 
                    String description, Position position){
        this.incidentOriginator = incidentOriginator;
        this.incidentID = incidentID;
        this.language = language;
        /*this.incidentCategory = incidentCategory;
        this.incidentType = incidentType;
        this.priority = priority;
        this.severity = severity;
        this.certainty = certainty;
        this.expirationTimeUTC = expirationTimeUTC;
        this.title = title;*/
        this.startTimeUTC = startTimeUTC;
        this.description = description;
        this.position = position;
    }
    
}
