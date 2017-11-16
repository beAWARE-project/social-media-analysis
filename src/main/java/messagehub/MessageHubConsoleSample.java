/**
 * Copyright 2015-2016 IBM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corp. 2015-2016
 */
package messagehub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import messagehub.bluemix.BluemixEnvironment;
import messagehub.bluemix.MessageHubCredentials;
import messagehub.rest.RESTAdmin;

/**
 * Console-based sample interacting with Message Hub, authenticating with SASL/PLAIN over an SSL connection.
 *
 * @author IBM
 */
public class MessageHubConsoleSample {

    private static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    private static final String APP_NAME = "kafka-java-console-sample-2.0";
    private static final String DEFAULT_TOPIC_NAME = "kafka-java-console-sample-topic";
    private static final String ARG_CONSUMER = "-consumer";
    private static final String ARG_PRODUCER_ = "-producer";
    private static final String ARG_TOPIC = "-topic";

    private static Thread consumerThread = null;
    private static ConsumerRunnable consumerRunnable = null;
    private static Thread producerThread = null;
    private static ProducerRunnable producerRunnable = null;
    private static String resourceDir;

    //add shutdown hooks (intercept CTRL-C etc.)
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown received.");
                shutdown();
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Uncaught Exception on " + t.getName() + " : " + e);
                shutdown();
            }
        });
    }

    private static void printUsage() {
        System.out.println("\n"
                + "Usage:\n"
                + "    java -jar build/libs/" + APP_NAME + ".jar \\\n"
                + "              <kafka_brokers_sasl> <kafka_admin_url> <api_key> [" + ARG_CONSUMER + "] \\\n"
                + "              [" + ARG_PRODUCER_ + "] [" + ARG_TOPIC + "]\n"
                + "Where:\n"
                + "    kafka_broker_sasl\n"
                + "        Required. Comma separated list of broker endpoints to connect to, for\n"
                + "        example \"host1:port1,host2:port2\".\n"
                + "    kafka_admin_url\n"
                + "        Required. The URL of the Message Hub Kafka administration REST endpoint.\n"
                + "    api_key\n"
                + "        Required. A Message Hub API key used to authenticate access to Kafka.\n"
                + "    " + ARG_CONSUMER + "\n"
                + "        Optional. Only consume message (do not produce messages to the topic).\n"
                + "        If omitted this sample will both produce and consume messages.\n"
                + "    " + ARG_PRODUCER_ + "\n"
                + "        Optional. Only produce messages (do not consume messages from the\n"
                + "        topic). If omitted this sample will both produce and consume messages.\n"
                + "    " + ARG_TOPIC + "\n"
                + "        Optional. Specifies the Kafka topic name to use. If omitted the\n"
                + "        default used is '" + DEFAULT_TOPIC_NAME + "'\n");
    }

    public static void main(String args[])  {
        try {
            final String userDir = System.getProperty("user.dir");
            final boolean isRunningInBluemix = BluemixEnvironment.isRunningInBluemix();
            final Properties clientProperties = new Properties();

            String bootstrapServers = null;
            String adminRestURL = null;
            String apiKey = null;
            boolean runConsumer = true;
            boolean runProducer = true;
            String topicName = DEFAULT_TOPIC_NAME;
            // Check environment: Bluemix vs Local, to obtain configuration parameters
            if (isRunningInBluemix) {

                //logger.log(Level.INFO, "Running in Bluemix mode.");
                System.out.println("Running in Bluemix mode.");
                resourceDir = userDir + File.separator + APP_NAME + File.separator + "bin" + File.separator + "resources";

                MessageHubCredentials credentials = BluemixEnvironment.getMessageHubCredentials();

                bootstrapServers = stringArrayToCSV(credentials.getKafkaBrokersSasl());
                adminRestURL = credentials.getKafkaRestUrl();
                apiKey = credentials.getApiKey();

                updateJaasConfiguration(credentials.getUser(), credentials.getPassword());

            } else {
                // If running locally, parse the command line
                /*if (args.length < 3) {
                    logger.log(Level.ERROR, "It appears the application is running outside of Bluemix but the arguments are incorrect for local mode.");
                    printUsage();
                    System.exit(-1);
                }*/
                System.out.println("Running in local mode.");
                //logger.log(Level.INFO, "Running in local mode.");
                resourceDir = userDir + File.separator + "resources";
                /*bootstrapServers = args[0];
                adminRestURL = args[1];
                apiKey = args[2];*/
                
                apiKey = "";
                adminRestURL = "";
                bootstrapServers = "";
                updateJaasConfiguration(apiKey.substring(0, 16), apiKey.substring(16));
                /*if (args.length > 3) {
                    try {
                        final ArgumentParser argParser = ArgumentParser.builder()
                                .flag(ARG_CONSUMER)
                                .flag(ARG_PRODUCER_)
                                .option(ARG_TOPIC)
                                .build();
                        final Map<String, String> parsedArgs =
                                argParser.parseArguments(Arrays.copyOfRange(args, 3, args.length));
                        if (parsedArgs.containsKey(ARG_CONSUMER) && !parsedArgs.containsKey(ARG_PRODUCER_)) {
                            runProducer = false;
                        }
                        if (parsedArgs.containsKey(ARG_PRODUCER_) && !parsedArgs.containsKey(ARG_CONSUMER)) {
                            runConsumer = false;
                        }
                        if (parsedArgs.containsKey(ARG_TOPIC)) {
                            topicName = parsedArgs.get(ARG_TOPIC);
                        }
                    } catch (IllegalArgumentException e) {
                        logger.log(Level.ERROR, e.getMessage());
                        System.exit(-1);
                    }
                }*/
                
            }
            //inject bootstrapServers in configuration, for both consumer and producer
            clientProperties.put("bootstrap.servers", bootstrapServers);
            System.out.println("Kafka Endpoints: " + bootstrapServers);
            System.out.println("Admin REST Endpoint: " + adminRestURL);
            //logger.log(Level.INFO, "Kafka Endpoints: " + bootstrapServers);
            //logger.log(Level.INFO, "Admin REST Endpoint: " + adminRestURL);
            
            //Using Message Hub Admin REST API to create and list topics
            //If the topic already exists, creation will be a no-op
            try {
                topicName = "social_media_text";
                System.out.println("Creating the topic " + topicName);
                //logger.log(Level.INFO, "Creating the topic " + topicName);
                String restResponse = RESTAdmin.createTopic(adminRestURL, apiKey, topicName);
                System.out.println("Admin REST response :" + restResponse);
                //logger.log(Level.INFO, "Admin REST response :" +restResponse);
                String topics = RESTAdmin.listTopics(adminRestURL, apiKey);
                System.out.println("Admin REST Listing Topics: " + topics);
                //logger.log(Level.INFO, "Admin REST Listing Topics: " + topics);
            } catch (Exception e) {
                System.out.println("Error occurred accessing the Admin REST API " + e);
                //logger.log(Level.ERROR, "Error occurred accessing the Admin REST API " + e, e);
                //The application will carry on regardless of Admin REST errors, as the topic may already exist
            }
            runConsumer = false;
            //create the Kafka clients
            if (runConsumer) {
                Properties consumerProperties = getClientConfiguration(clientProperties, "consumer.properties");
                consumerRunnable = new ConsumerRunnable(consumerProperties, topicName);
                consumerThread = new Thread(consumerRunnable, "Consumer Thread");
                consumerThread.start();
            }
            runProducer = true;
            if (runProducer) {
                Properties producerProperties = getClientConfiguration(clientProperties, "producer.properties");
                producerRunnable = new ProducerRunnable(producerProperties, topicName);
                producerThread = new Thread(producerRunnable, "Producer Thread");
                producerThread.start();
            }

            System.out.println("MessageHubConsoleSample will run until interrupted.");
            //logger.log(Level.INFO, "MessageHubConsoleSample will run until interrupted.");
        } catch (Exception e) {
            System.out.println("Exception occurred, application will terminate");
            //logger.log(Level.ERROR, "Exception occurred, application will terminate", e);
            System.exit(-1);
        }
    }

    /*
     * convenience method for cleanup on shutdown
     */
    private static void shutdown() {
        if (producerRunnable != null)
            producerRunnable.shutdown();
        if (consumerRunnable != null)
            consumerRunnable.shutdown();
        if (producerThread != null)
            producerThread.interrupt();
        if (consumerThread != null)
            consumerThread.interrupt();
    }


    /*
     * Return a CSV-String from a String array
     */
    private static String stringArrayToCSV(String[] sArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sArray.length; i++) {
            sb.append(sArray[i]);
            if (i < sArray.length -1) sb.append(",");
        }
        return sb.toString();
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
        
        System.setProperty(JAAS_CONFIG_PROPERTY, jaasConfPath);
        
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
