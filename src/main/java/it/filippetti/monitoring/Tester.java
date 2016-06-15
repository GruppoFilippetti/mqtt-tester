package it.filippetti.monitoring;

import it.filippetti.monitoring.commands.CliManager;
import it.filippetti.monitoring.listeners.MessagesListener;
import it.filippetti.monitoring.mqtt.MQTTManager;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Francesco on 14/06/2016.
 */
public class Tester implements MessagesListener {

    private static final Logger logger = Logger.getLogger(Tester.class.getName());
    private long lastPublishedMessageTimestamp = -1;
    private MQTTManager mqttManager;


    public Tester() {
        mqttManager = new MQTTManager();
        mqttManager.addListener(this);
    }

    public void messageReceived(long timestamp) {
        long lastPubl = this.lastPublishedMessageTimestamp; // WAS:: mqttManager.getLastPublishedMessageTimestamp();
        long lastRecv = timestamp;                          // WAS:: mqttManager.getLastReceivedMessageTimestamp();
        if (lastPubl>=0 && lastRecv>=0) {
            long timePassed = lastRecv - lastPubl;
            logger.log(Level.INFO, "Time passed in milliseconds: " + timePassed + " (" + lastPubl + "-->" + lastRecv + ")");
            System.out.println(timePassed);
        } else {
            logger.log(Level.SEVERE, "Sorry, could not determine how much time is passed from message PUBLISH to RECEIVE.");
            System.out.println(-1);
            System.exit(-1);
        }

        // TODO: DISCONNECTION
//        try {
//            mqttManager.disconnectClient();
//        } catch (MqttException e) {
//            logger.log(Level.SEVERE, "MQTT Exception", e);
//            System.out.println(-1);
//            System.exit(-1);
//        }

        System.exit(0);
    }


    public void makeTest(String[] args) {

        // Parse input arguments
        CliManager cli = new CliManager(args);
        boolean parsed = cli.parse();

        // Disable logs unless otherwise specified
        if (!cli.getLoggingEnabled())
            LogManager.getLogManager().reset();

        if (!parsed) {
            logger.log(Level.SEVERE, "Parsing ended unexpectedly.");
            System.out.println(-1);
            System.exit(-1);
        }

        // Create client
        String mqttURI = cli.getMqttURI();
        String clientName = cli.getClient();
        boolean created = mqttManager.createClient(mqttURI, clientName);
        if (!created) {
            logger.log(Level.SEVERE, "Could not create client");
            System.out.println(-1);
            System.exit(-1);
        }

        // Build MQTT options for client connection
        final MqttConnectOptions mqttOptions = new MqttConnectOptions();
        mqttOptions.setCleanSession(cli.getCleanSession());
        mqttOptions.setKeepAliveInterval(cli.getKeepAlive());
        if (cli.getUserName()!=null)
            mqttOptions.setUserName(cli.getUserName());
        if (cli.getPassword()!=null)
            mqttOptions.setPassword(cli.getPassword());

        try {

            // Connect client
            boolean connected = mqttManager.connectClient(mqttOptions);
            if (!connected) {
                logger.log(Level.SEVERE, "Could not connect to client");
                System.out.println(-1);
                System.exit(-1);
            }

//            Runtime.getRuntime().addShutdownHook(new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        System.out.println("Shutdown hook");
//                        mqttManager.disconnectClient();
//                    } catch (Throwable e) {
//                        logger.log(Level.SEVERE, "Unknown Exception", e);
//                    }
//                }
//            });

            // Subscribe to fake topic
            boolean subscribed = mqttManager.subscribeToTopic(cli.getTopic());
            if (!subscribed) {
                logger.log(Level.SEVERE, "Could not subscribe to topic");
                System.out.println(-1);
                System.exit(-1);
            }

            // Publish fake message onto fake topic
            long msgOutTimestamp = System.currentTimeMillis();
            this.lastPublishedMessageTimestamp = msgOutTimestamp;
            boolean published = mqttManager.publishOverTopic(cli.getTopic(), cli.getMessage());
            if (!published) {
                logger.log(Level.SEVERE, "Could not publish over topic");
                System.out.println(-1);
                System.exit(-1);
            }

            // If everything goes well, the method "messageReceived()" gets called and the following lines will not be executed

            // Sleep for the maximum amount of milliseconds we can wait for a message to arrive, then disconnect
            Thread.sleep(cli.getWaiting());
            logger.log(Level.INFO, "Client has not received a message within " + cli.getWaiting() + " milliseconds. Too much time has passed.");
            mqttManager.disconnectClient();

        } catch (MqttException e) {
            logger.log(Level.SEVERE, "MQTT Exception", e);
            System.out.println(-1);
            System.exit(-1);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted.", e);
            System.out.println(-1);
            System.exit(-1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unknown error.", e);
            System.out.println(-1);
            System.exit(-1);
        }
    }


    public static void main(String[] args) {
        new Tester().makeTest(args);
    }

}
