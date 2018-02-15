/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author andreadisst
 */
public class TwitterReport {
    
    public static String getDummyMessage(String collection){
        
        String twitterReport = "", description = "";
        switch(collection){
            case "EnglishFloods": twitterReport = "TwitterReport1516098723965.html";
                                  description = "flooded river flooding rain bridge";
                                  break;
            case "ItalianFloods": twitterReport = "TwitterReport1518006170454.html";
                                  description = "allagato fiume allagamento pioggia ponte";
                                  break;
        }

        return "{\n" +
        "    \"header\": {\n" +
        "        \"topicName\": \"TOP021_INCIDENT_REPORT\",\n" +
        "        \"topicMajorVersion\": 1,\n" +
        "        \"topicMinorVersion\": 0,\n" +
        "        \"sender\": \"SMA\",\n" +
        "        \"msgIdentifier\": \"SMA00000001\",\n" +
        "        \"sentUTC\": \"2018-01-01T12:00:00Z\",\n" +
        "        \"status\": \"Actual\",\n" +
        "        \"actionType\": \"Update\",\n" +
        "        \"specificSender\": \"\",\n" +
        "        \"scope\": \"\",\n" +
        "        \"district\": \"Thessaloniki\",\n" +
        "        \"recipients\": \"\",\n" +
        "        \"code\": 0,\n" +
        "        \"note\": \"\",\n" +
        "        \"references\": \"\"\n" +
        "    },\n" +
        "    \"body\": {\n" +
        "        \"incidentOriginator\": \"SMA\",\n" +
        "        \"incidentID\": \"432115\",\n" +
        "        \"language\": \"en-US\",\n" +
        "        \"incidentCategory\": \"Other\",\n" +
        "        \"incidentType\": \"Other\",\n" +
        "        \"priority\": \"undefined\",\n" +
        "        \"severity\": \"Unknown\",\n" +
        "        \"certainty\": \"Observed\",\n" +
        "        \"startTimeUTC\": \"2018-01-01T12:00:00Z\",\n" +
        "        \"expirationTimeUTC\": \"2018-01-01T12:00:00Z\",\n" +
        "        \"title\": \"\",\n" +
        "        \"description\": \"" + description + "\",\n" +
        "        \"position\": {\n" +
        "            \"latitude\": 45.5455,\n" +
        "            \"longitude\": 11.5354\n" +
        "        },\n" +
        "        \"attachments\": [{\n" +
        "                \"attachmentName\": \""+twitterReport+"\",\n" +
        "                \"attachmentType\": \"webpage\",\n" +
        "                \"attachmentTimeStampUTC\": \"2018-01-01T12:00:00Z\",\n" +
        "                \"attachmentURL\": \"http://object-store-app.eu-gb.mybluemix.net/objectStorage?file="+twitterReport+"\"\n" +
        "            }\n" +
        "        ]\n" +
        "    }\n" +
        "}";
    }
    
    public static String generateReport(ArrayList<TwitterReportLine> twitterReportLines) throws IOException{
        
        String filename = "TwitterReport"+System.currentTimeMillis()+".html";
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        
        for(TwitterReportLine twitterReportLine : twitterReportLines){
            String line = "<div><p><b>"+twitterReportLine.getText()+"</b></p><p><i>posted by</i> "+
                    twitterReportLine.getUser()+" <i>at</i> "+twitterReportLine.getDate()+"</p><p><a href=\"https://twitter.com/statuses/"+
                    twitterReportLine.getId()+"\">View on Twitter</a></div><br>";
            writer.write(line);
        }
        
        writer.close();
        
        CDR.storeFile(filename, filename);
        
        File file = new File(filename);
        file.delete();
        
        return filename;
    }
}
