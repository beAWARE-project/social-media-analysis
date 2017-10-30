/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mykafka;

import crawler.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;


/**
 *
 * @author andreadisst
 */
public class Bus {
    
    private static String resourceDir;
    private static KafkaProducer<String, String> kafkaProducer;
    
    public Bus(){
        
        String userDir = System.getProperty("user.dir");
        String api_key = System.getenv("SECRET_MH_API_KEY");
        String kafka_brokers_sasl = System.getenv("SECRET_MH_BROKERS");
        System.out.println("check environment variables:\n"+api_key+"\n"+kafka_brokers_sasl);
        Properties clientProperties = new Properties();
        
        resourceDir = userDir + File.separator + "resources";
        
        try{
            updateJaasConfiguration(api_key.substring(0, 16), api_key.substring(16));
        }catch(IOException e){
            System.out.println("Exception during bus configuration: " + e);
        }
        
        clientProperties.put("bootstrap.servers", kafka_brokers_sasl);
        //System.out.println("Kafka Endpoints: " + kafka_brokers_sasl);
        //System.out.println("Admin REST Endpoint: " + Configuration.kafka_admin_url);
        
        Properties producerProperties = getClientConfiguration(clientProperties, "producer.properties");
        kafkaProducer = new KafkaProducer<>(producerProperties);
        
    }
    
    public void post(String topicName, String message) throws IOException, InterruptedException, ExecutionException, TimeoutException{
        
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName,Configuration.key,message);
                    
        Future<RecordMetadata> future = kafkaProducer.send(record);
        RecordMetadata recordMetadata = future.get(1000, TimeUnit.MILLISECONDS);
        //System.out.println("Message produced, offset: " + recordMetadata.offset() + ", partition : " + recordMetadata.partition() + ", topic: " + recordMetadata.topic());
        System.out.println("Sent #" + recordMetadata.offset() + " message: " + message);

    }
    
    public void close(){
        kafkaProducer.close(1000, TimeUnit.MILLISECONDS);
    }
    
    /*
     * Retrieve client configuration information, using a properties file, for
     * connecting to Message Hub Kafka.
     */
    static final Properties getClientConfiguration(Properties commonProps, String fileName) {
        Properties result = new Properties();
        InputStream propsStream;

        try {
            propsStream = new FileInputStream(resourceDir + File.separator + fileName);
            result.load(propsStream);
            propsStream.close();
        } catch (IOException e) {
            System.out.println("Could not load properties from file");
            return result;
        }

        result.putAll(commonProps);
        return result;
    }

    /*
     * Updates JAAS config file with provided credentials.
     */
    private static void updateJaasConfiguration(String username, String password) throws IOException {
        // Set JAAS configuration property.
        String jaasConfPath = System.getProperty("java.io.tmpdir") + File.separator + "jaas.conf";
        
        System.setProperty(Configuration.JAAS_CONFIG_PROPERTY, jaasConfPath);
        
        String templatePath = resourceDir + File.separator + "jaas.conf.template";
        
        OutputStream jaasOutStream = null;

        System.out.println("Updating JAAS configuration");

        try {
            String templateContents = new String(Files.readAllBytes(Paths.get(templatePath)));
            jaasOutStream = new FileOutputStream(jaasConfPath, false);

            // Replace username and password in template and write
            // to jaas.conf in resources directory.
            String fileContents = templateContents
                    .replace("$USERNAME", username)
                    .replace("$PASSWORD", password);

            jaasOutStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
        } catch (final IOException e) {
            System.out.println("Failed accessing to JAAS config file");
            throw e;
        } finally {
            if (jaasOutStream != null) {
                try {
                    jaasOutStream.close();
                } catch(final Exception e) {
                    System.out.println("Error closing generated JAAS config file");
                }
            }
        }
    }
    
}
