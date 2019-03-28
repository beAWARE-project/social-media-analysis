/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classification;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author andreadisst
 */
public class Validation {
    
    private static final String[] EMOTICONS = {":‑)",":)",":-]",":]",":-3",":3",":->",":>","8-)","8)",":-}",":}",":o)",":c)",":^)","=]","=)", //smiley or happy face
                                  ":‑D"," :D ","8‑D","8D"," x‑D "," xD "," X‑D "," XD "," =D "," =3 ","B^D", //laughing, big grin, laugh with glasses, or wide-eyed surprise
                                  ":'‑)",":')", //tears of happiness
                                  ":-*",":*",":×", //kiss
                                  ";‑)"," ;)","*-)","*)",";‑]",";]",";^)",":‑,"," ;D", //wink, smirk
                                  ":‑P"," :P"," X‑P "," XP "," x‑p "," xp ",":‑p"," :p ",":‑Þ",":Þ",":‑þ",":þ",":‑b"," :b "," d: "," =p ",">:P",  //tongue sticking out, cheeky/playful, blowing a raspberry
                                  "<3" //heart
                                 };
    
    private static final String[] EMOJIS = {"☺","️","🙂","😊","😀","😁", //smiley or happy face
                               "😃","😄","😆","😍", //laughing, big grin, laugh with glasses, or wide-eyed surprise
                               "😂", //tears of happiness
                               "😗","😙","😚","😘","😍", //kiss
                               "😉","😜","😘", //wink, smirk
                               "😛","😝","😜","🤑", //tongue sticking out, cheeky/playful, blowing a raspberry
                               "❤" //heart
                              };
            
    /*public static void main(String[] args) {
        String x = "";
        System.out.println(EmoticonsEstimation(x));
    }*/
    
    public static boolean EmoticonsEstimation(String text){
        for(String emoticon : EMOTICONS){
            if(text.contains(emoticon)){
                return false;
            }
        }
        for(String emoji : EMOJIS){
            if(text.contains(emoji)){
                return false;
            }
        }
        return true;
    }
    
    public static VerificationResponse verifyTweet(String tweet){
        
        boolean predicted_value = true;
        Double confidence_value = 0.0;
        
        try {
            
            StringEntity entity = new StringEntity(tweet,ContentType.APPLICATION_FORM_URLENCODED);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("http://160.40.49.111:9015/verify");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                try{
                    if(!httpEntity.equals("null")){
                        String output = EntityUtils.toString(httpEntity);
                        JsonObject obj = new JsonParser().parse(output).getAsJsonObject();
                        confidence_value = obj.get("confidence_value").getAsDouble();
                        if( obj.get("predicted_value").getAsString().equals("fake") ){
                            predicted_value = false;
                        }
                    }
                } catch (JsonSyntaxException | IOException e){
                    System.out.println("Error: " + e);
                }
            }
            
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        
        return new VerificationResponse(predicted_value,confidence_value);
    }
    
}
