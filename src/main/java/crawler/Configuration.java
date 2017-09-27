/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author andreadisst
 */
public class Configuration {
    
    public static String consumerKey = "TsOX1TnaRusWLYPsPBxw1sCph";
    public static String consumerSecret = "cUIAcryGfDPy8AxxhVWHRgMw0AK8jVzX7KqxF0E72SwmfTLBaW";
    public static String token = "835244605557833729-Wl4bFKe2852FY03ED9OUxqBURZ7hwvZ";
    public static String secret = "6NNB4Fu4EStIQS2oU9qG7DV61hL1wg4AXivPd5CtW7WZ1";
    
    public static List<String> languages = Lists.newArrayList("English", "Greek", "Italian", "Spanish", "Danish");
    public static List<String> pilots = Lists.newArrayList("Fires", "Floods", "Heatwave");
    
    public static String database = "BeAware";
    public static String host = "160.40.49.112";
    public static int port = 27017;
    public static String username = "beaware!user";
    public static String password = "?bE@WWar3%";
    
    public static String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    public static String key = "key";
    public static String api_key = "BPWTh17zQ2kDvxuvmSoHqZEHEbbu6izktAHKC8aD2EGDVNeO";
    public static String kafka_admin_url = "https://kafka-admin-prod02.messagehub.services.eu-gb.bluemix.net:443";
    public static String kafka_brokers_sasl = "kafka03-prod02.messagehub.services.eu-gb.bluemix.net:9093" +
        "kafka02-prod02.messagehub.services.eu-gb.bluemix.net:9093," +
        "kafka04-prod02.messagehub.services.eu-gb.bluemix.net:9093," +
        "kafka05-prod02.messagehub.services.eu-gb.bluemix.net:9093," +
        "kafka01-prod02.messagehub.services.eu-gb.bluemix.net:9093";
    
    public static String socialMediaText = "social_media_text";
    public static String socialMediaTextDemo = "social_media_text_demo";
    
    public static List<String> old_collections = Lists.newArrayList("Association", "Image", "Post", "UserAccount", "Webpage");
    public static String old_host = "160.40.51.213";
    public static String old_username = "tensor";
    public static String old_password = "S4n^t0P";
    
    public static String local_host = "127.0.0.1";
}
