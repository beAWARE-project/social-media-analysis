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
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author andreadisst
 */
public class TwitterReport {
    
    public static String getDummyMessage(String collection){
        
        String twitterReport = "", description = "", id="", district="", position_x="", position_y="";
        switch(collection){
            case "EnglishFloods": twitterReport = "TwitterReport1516098723965.html";
                                  description = "flooded river flooding rain bridge";
                                  id = "001";
                                  district = "Vicenza";
                                  position_x="45.5455";
                                  position_y="11.5354";
                                  break;
            case "ItalianFloods": twitterReport = "TwitterReport1518006170454.html";
                                  description = "allagato fiume allagamento pioggia ponte";
                                  id = "002";
                                  district = "Vicenza";
                                  position_x="45.5455";
                                  position_y="11.5354";
                                  break;
            case "SpanishFires":  twitterReport = "TwitterReport1519305864751.html";
                                  description = "ultima hora camiones fuego humo";
                                  id = "003";
                                  district = "Valencia";
                                  position_x="39.4699";
                                  position_y="0.3763";
                                  break;    
            case "GreekHeatwave": twitterReport = "TwitterReport1519305940233.html";
                                  description = "ζέστη κλιματιζόμενος χώρος φανάρια ρεύμα";
                                  id = "004";
                                  district = "Thessaloniki";
                                  position_x="40.6401";
                                  position_y="22.9444";
                                  break;
        }
        
        LocalDateTime ldt = LocalDateTime.now();
        String now = ldt.withNano(0) + "Z";
        
        return "{\n" +
        "    \"header\": {\n" +
        "        \"topicName\": \"TOP021_INCIDENT_REPORT\",\n" +
        "        \"topicMajorVersion\": 1,\n" +
        "        \"topicMinorVersion\": 0,\n" +
        "        \"sender\": \"SMA\",\n" +
        "        \"msgIdentifier\": \"SMAmsg_"+id+"\",\n" +
        "        \"sentUTC\": \""+now+"\",\n" +
        "        \"status\": \"Actual\",\n" +
        "        \"actionType\": \"Alert\",\n" +
        "        \"specificSender\": \"\",\n" +
        "        \"scope\": \"Public\",\n" +
        "        \"district\": \""+district+"\",\n" +
        "        \"recipients\": \"\",\n" +
        "        \"code\": 0,\n" +
        "        \"note\": \"\",\n" +
        "        \"references\": \"\"\n" +
        "    },\n" +
        "    \"body\": {\n" +
        "        \"incidentOriginator\": \"SMA\",\n" +
        "        \"incidentID\": \"SMAinc_"+id+"\",\n" +
        "        \"language\": \"en-US\",\n" +
        "        \"incidentCategory\": \"Other\",\n" +
        "        \"incidentType\": \"Other\",\n" +
        "        \"priority\": \"undefined\",\n" +
        "        \"severity\": \"Unknown\",\n" +
        "        \"certainty\": \"Observed\",\n" +
        "        \"startTimeUTC\": \""+now+"\",\n" +
        "        \"expirationTimeUTC\": \"2019-01-01T12:00:00Z\",\n" +
        "        \"title\": \"\",\n" +
        "        \"description\": \"" + description + "\",\n" +
        "        \"position\": {\n" +
        "            \"latitude\": "+position_x+",\n" +
        "            \"longitude\": "+position_y+"\n" +
        "        },\n" +
        "        \"attachments\": [{\n" +
        "                \"attachmentName\": \""+twitterReport+"\",\n" +
        "                \"attachmentType\": \"webpage\",\n" +
        "                \"attachmentTimeStampUTC\": \""+now+"\",\n" +
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
