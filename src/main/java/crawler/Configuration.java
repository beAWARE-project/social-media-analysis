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

    public static String consumerKey = "";
    public static String consumerSecret = "";
    public static String token = "";
    public static String secret = "";

    public static List<String> languages = Lists.newArrayList("English", "Greek", "Italian", "Spanish", "Danish");
    public static List<String> pilots = Lists.newArrayList("Fires", "Floods", "Heatwave");

    public static String database = "";
    public static String host = "";
    public static int port = 27017;
    public static String username = "";
    public static String password = "";

    public static String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    public static String key = "key";
    public static String kafka_admin_url = "https://kafka-admin-prod02.messagehub.services.eu-gb.bluemix.net:443";

    public static String socialMediaText001 = "TOP001_SOCIAL_MEDIA_TEXT";
    public static String socialMediaText = "social_media_text";
    public static String socialMediaTrigger = "TOP000_SOCIAL_MEDIA_TRIGGER";
    public static String incidentTopic021 = "TOP021_INCIDENT_REPORT";

    public static List<String> old_collections = Lists.newArrayList("Association", "Image", "Post", "UserAccount", "Webpage");
    public static String old_host = "";
    public static String old_username = "";
    public static String old_password = "";

    public static String local_host = "127.0.0.1";
    
    public static String DATA_STORAGE_URL = "https://beaware.uk-south.containers.mybluemix.net/object-store/";
}
