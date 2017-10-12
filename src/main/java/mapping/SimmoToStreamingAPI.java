/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import crawler.Configuration;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author andreadisst
 */
public class SimmoToStreamingAPI {
    
    public static void main(String[] args) throws UnknownHostException{
        
        MongoClient mongoClient = new MongoClient( Configuration.host , Configuration.port);
        
        /*DB toDB = mongoClient.getDB("test1");
        toDB.authenticate(Configuration.old_username, Configuration.old_password.toCharArray());
        DBCollection toTable = toDB.getCollection("Test");*/
        
        DB toDB = mongoClient.getDB("BeAware");
        toDB.authenticate(Configuration.username, Configuration.password.toCharArray());
        DBCollection toTable = toDB.getCollection("DanishHeatwave");
        
        DB db = mongoClient.getDB("BeAwareDanishHeatwave");
        db.authenticate(Configuration.old_username, Configuration.old_password.toCharArray());
        DBCollection table = db.getCollection("Post");
        DBCursor cursor = table.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT).skip(0).limit(200000);
        while (cursor.hasNext()) {
            DBObject post = cursor.next();
            BasicDBObject newPost = new BasicDBObject();
            
            String _id = post.get("_id").toString();
            String id_str = _id.replace("Twitter#", "");
            
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("id_str", id_str);
            DBObject existingPost = toTable.findOne(searchQuery);
            if(existingPost==null){
                newPost.append("id_str", id_str);
                newPost.append("id", Long.parseLong(id_str));
                if(post.containsField("language")){
                    newPost.append("lang", post.get("language").toString());
                }
                if(post.containsField("location")){
                    DBObject location = (BasicDBObject) post.get("location");
                    if(location.containsField("country")){
                        BasicDBObject place = new BasicDBObject();
                        place.append("country", location.get("country").toString());
                        newPost.append("place", place);
                    }
                }
                if(post.containsField("numLikes")){
                    newPost.append("favorite_count", Integer.parseInt(post.get("numLikes").toString()));
                }
                if(post.containsField("numShares")){
                    newPost.append("retweet_count", Integer.parseInt(post.get("numShares").toString()));
                }
                if(post.containsField("title")){
                    newPost.append("text", post.get("title").toString());
                }
                if(post.containsField("creationDate")){
                    Date creationDate = (Date) post.get("creationDate");
                    TimeZone.setDefault(TimeZone.getTimeZone("+0000"));
                    newPost.append("created_at", creationDate.toString().replace("GMT", "+0000"));
                    newPost.append("timestamp_ms", String.valueOf(creationDate.getTime()));
                }
                if(post.containsField("relevant")){
                    newPost.append("relevant", (boolean) post.get("relevant"));
                }
                if(post.containsField("estimatedRelevant")){
                    newPost.append("estimated_relevancy", (boolean) post.get("estimatedRelevant"));
                }
                newPost.append("from_simmo", true);
                BasicDBObject entities = new BasicDBObject();
                if(post.containsField("tags")){
                    List<BasicDBObject> hashtags = new ArrayList<>();
                    List<String> tags = (List<String>) post.get("tags");
                    for(String tag : tags){
                        BasicDBObject hashtag = new BasicDBObject();
                        hashtag.append("text", tag);
                        hashtags.add(hashtag);
                    }
                    entities.append("hashtags", hashtags);
                }
                if(post.containsField("replied")){
                    DBRef replied = (DBRef) post.get("replied");
                    String replied_$id = replied.getId().toString();
                    String in_reply_to_user_id_str = replied_$id.replace("Twitter#", "");
                    newPost.append("in_reply_to_user_id_str", in_reply_to_user_id_str);
                    newPost.append("in_reply_to_user_id", Long.parseLong(in_reply_to_user_id_str));
                    DBCollection UserAccount = db.getCollection("UserAccount");
                    searchQuery = new BasicDBObject();
                    searchQuery.put("_id", replied_$id);
                    DBObject user = UserAccount.findOne(searchQuery);
                    if(user!=null){
                        if(user.containsField("username")){
                            newPost.append("in_reply_to_screen_name", user.get("username").toString());
                        }
                    }
                }

                if(post.containsField("contributor")){
                    DBRef contributor = (DBRef) post.get("contributor");
                    String contributor_$id = contributor.getId().toString();
                    searchQuery = new BasicDBObject();
                    searchQuery.put("_id", contributor_$id);
                    DBCollection UserAccount = db.getCollection("UserAccount");
                    DBObject user = UserAccount.findOne(searchQuery);
                    if(user!=null){
                        BasicDBObject newUser = new BasicDBObject();
                        newUser.append("id_str", contributor_$id.replace("Twitter#", ""));
                        newUser.append("id", Long.parseLong(contributor_$id.replace("Twitter#", "")));
                        if(user.containsField("description"))
                            newUser.append("description", user.get("description").toString());
                        if(user.containsField("isVerified"))
                            newUser.append("verified", (boolean) user.get("isVerified"));
                        if(user.containsField("location"))
                            newUser.append("location", user.get("location").toString());
                        if(user.containsField("name"))
                            newUser.append("name", user.get("name").toString());
                        if(user.containsField("username"))
                            newUser.append("sceen_name", user.get("username").toString());
                        if(user.containsField("numFavourites"))
                            newUser.append("favourites_count", (int) user.get("numFavourites"));
                        if(user.containsField("numFollowers"))
                            newUser.append("followers_count", (int) user.get("numFollowers"));
                        if(user.containsField("numFriends"))
                            newUser.append("friends_count", (int) user.get("numFriends"));
                        if(user.containsField("numItems"))
                            newUser.append("statuses_count", (int) user.get("numItems"));
                        if(user.containsField("numListed"))
                            newUser.append("listed_count", (int) user.get("numListed"));
                        if(user.containsField("avatarBig")){
                            newUser.append("profile_image_url", user.get("avatarBig").toString());
                            newUser.append("profile_image_url_https", user.get("avatarBig").toString().replace("http:","https:"));
                        }
                        if(user.containsField("creationDate")){
                            Date user_creationDate = (Date) user.get("creationDate");
                            TimeZone.setDefault(TimeZone.getTimeZone("+0000"));
                            newUser.append("created_at", user_creationDate.toString().replace("GMT", "+0000"));
                        }
                        newPost.append("user", newUser);
                    }
                }

                DBCollection Webpage = db.getCollection("Webpage");
                searchQuery = new BasicDBObject();
                searchQuery.put("_id", _id);
                DBCursor webpage_cursor = Webpage.find(searchQuery);
                List<BasicDBObject> urls = new ArrayList<>();
                while (webpage_cursor.hasNext()) {
                    BasicDBObject url = new BasicDBObject();
                    url.append("url", webpage_cursor.next().get("url").toString());
                    urls.add(url);
                }
                if(urls.size()>0)
                    entities.append("urls", urls);

                DBCollection Image = db.getCollection("Image");
                searchQuery = new BasicDBObject();
                searchQuery.put("sourceDocumentId", _id);
                DBCursor image_cursor = Image.find(searchQuery);
                List<BasicDBObject> medias = new ArrayList<>();
                while (image_cursor.hasNext()) {
                    BasicDBObject media = new BasicDBObject();
                    DBObject image = image_cursor.next();
                    media.append("id_str", image.get("_id").toString().replace("Twitter#", ""));
                    media.append("id", Long.parseLong(image.get("_id").toString().replace("Twitter#", "")));
                    if(image.containsField("url")){
                        media.append("media_url", image.get("url").toString());
                        media.append("media_url_https", image.get("url").toString().replace("http:", "https:"));
                    }
                    if(image.containsField("webPageUrl"))
                        media.append("expanded_url", image.get("webPageUrl").toString());
                    media.append("type", "photo");
                    BasicDBObject large = new BasicDBObject();
                    if(image.containsField("width"))
                        large.append("w", (int) image.get("width"));
                    if(image.containsField("height"))
                        large.append("h", (int) image.get("height"));
                    if(large.containsField("h")||large.containsField("w")){
                        BasicDBObject sizes = new BasicDBObject();
                        sizes.append("large", large);
                        media.append("sizes", sizes);
                    }
                    medias.add(media);
                }
                if(medias.size()>0)
                    entities.append("media", medias);

                DBCollection Association = db.getCollection("Association");
                searchQuery = new BasicDBObject();
                searchQuery.put("className", "gr.iti.mklab.simmo.core.associations.Mention");
                searchQuery.put("one.$id", _id);
                DBCursor association_cursor = Association.find(searchQuery);
                List<BasicDBObject> user_mentions = new ArrayList<>();
                while (association_cursor.hasNext()) {
                    BasicDBObject user_mention = new BasicDBObject();
                    DBRef other = (DBRef) association_cursor.next().get("other");
                    String other_$id = other.getId().toString();
                    user_mention.append("id", Long.parseLong(other_$id.replace("Twitter#", "")));
                    user_mention.append("id_str", other_$id.replace("Twitter#", ""));
                    DBCollection UserAccount = db.getCollection("UserAccount");
                    searchQuery = new BasicDBObject();
                    searchQuery.put("_id", other_$id);
                    DBObject user = UserAccount.findOne(searchQuery);
                    if(user!=null){
                        if(user.containsField("name"))
                            user_mention.append("name", user.get("name").toString());
                        if(user.containsField("screen_name"))
                            user_mention.append("screen_name", user.get("username").toString());
                    }
                    user_mentions.add(user_mention);
                }
                if(user_mentions.size()>0)
                    entities.append("user_mentions", user_mentions);
                newPost.append("entities", entities);

                searchQuery = new BasicDBObject();
                searchQuery.put("className", "gr.iti.mklab.simmo.core.associations.Interaction");
                searchQuery.put("interactionType", "RETWEET");
                searchQuery.put("one.$id", _id);
                DBObject association = Association.findOne(searchQuery);
                if(association!=null){
                    DBRef other = (DBRef) association.get("other");
                    String other_$id = other.getId().toString();
                    BasicDBObject newUser = new BasicDBObject();
                    newUser.append("id", Long.parseLong(other_$id.replace("Twitter#", "")));
                    newUser.append("id_str", other_$id.replace("Twitter#", ""));
                    DBCollection UserAccount = db.getCollection("UserAccount");
                    searchQuery = new BasicDBObject();
                    searchQuery.put("_id", other_$id);
                    DBObject user = UserAccount.findOne(searchQuery);
                    if(user!=null){
                        if(user.containsField("description"))
                            newUser.append("description", user.get("description").toString());
                        if(user.containsField("isVerified"))
                            newUser.append("verified", (boolean) user.get("isVerified"));
                        if(user.containsField("location"))
                            newUser.append("location", user.get("location").toString());
                        if(user.containsField("name"))
                            newUser.append("name", user.get("name").toString());
                        if(user.containsField("username"))
                            newUser.append("sceen_name", user.get("username").toString());
                        if(user.containsField("numFavourites"))
                            newUser.append("favourites_count", (int) user.get("numFavourites"));
                        if(user.containsField("numFollowers"))
                            newUser.append("followers_count", (int) user.get("numFollowers"));
                        if(user.containsField("numFriends"))
                            newUser.append("friends_count", (int) user.get("numFriends"));
                        if(user.containsField("numItems"))
                            newUser.append("statuses_count", (int) user.get("numItems"));
                        if(user.containsField("numListed"))
                            newUser.append("listed_count", (int) user.get("numListed"));
                        if(user.containsField("avatarBig")){
                            newUser.append("profile_image_url", user.get("avatarBig").toString());
                            newUser.append("profile_image_url_https", user.get("avatarBig").toString().replace("http:","https:"));
                        }
                        if(user.containsField("creationDate")){
                            Date user_creationDate = (Date) user.get("creationDate");
                            TimeZone.setDefault(TimeZone.getTimeZone("+0000"));
                            newUser.append("created_at", user_creationDate.toString().replace("GMT", "+0000"));
                        }
                    }
                    BasicDBObject retweeted_status = new BasicDBObject();
                    retweeted_status.append("user", newUser);
                    newPost.append("retweeted_status", retweeted_status);
                }
            
                toTable.insert(newPost);
            }
        }
    }
    
}
