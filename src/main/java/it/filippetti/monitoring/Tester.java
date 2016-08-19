package it.filippetti.monitoring;

import it.filippetti.monitoring.commands.CliManager;
import it.filippetti.monitoring.listeners.MessagesListener;
import it.filippetti.monitoring.mqtt.MQTTManager;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.concurrent.*;
import java.util.logging.LogManager;

/**
 * Created by Francesco on 14/06/2016.
 */
public class Tester implements MessagesListener {

    private long lastPublishedMessageTimestamp = -1;
    private MQTTManager mqttManager;
    private boolean loggingEnabled = false;

    private long duration;

    public Tester() {
        mqttManager = new MQTTManager();
        mqttManager.addListener(this);
    }

    public void messageReceived(long timestamp) {
        long lastPubl = this.lastPublishedMessageTimestamp; // WAS:: mqttManager.getLastPublishedMessageTimestamp();
        long lastRecv = timestamp;                          // WAS:: mqttManager.getLastReceivedMessageTimestamp();
        if (lastPubl>=0 && lastRecv>=0) {
            duration = lastRecv - lastPubl;
//            System.out.println(duration);
        } else {
            fail();
            duration = -1;
        }
//        System.exit(0);
    }

    public long getDuration() {
        return duration;
    }

    public void makeTest(String[] args) {
        try {

            // Parse input arguments
            CliManager cli = new CliManager(args);
            boolean parsed = cli.parse();
            if (!parsed) {
                //System.out.println(-1);
                System.exit(-1);
            }

            loggingEnabled = cli.getLoggingEnabled();

            // Create client
            int connectionTimeout = cli.getKeepAlive();
            String mqttURI = cli.getMqttURI();
            String clientName = cli.getClient();
            mqttManager.createClient(mqttURI, clientName);

            // Build MQTT options for client connection
            final MqttConnectOptions mqttOptions = new MqttConnectOptions();
            mqttOptions.setConnectionTimeout(connectionTimeout);
            mqttOptions.setCleanSession(cli.getCleanSession());
            mqttOptions.setKeepAliveInterval(cli.getKeepAlive());
            if (cli.getUserName()!=null)
                mqttOptions.setUserName(cli.getUserName());
            if (cli.getPassword()!=null)
                mqttOptions.setPassword(cli.getPassword());


            // Connect client
            mqttManager.connectClient(mqttOptions);

            // Subscribe to fake topic
            mqttManager.subscribeToTopic(cli.getTopic());

            // Publish fake message onto fake topic
            long msgOutTimestamp = System.currentTimeMillis();
            this.lastPublishedMessageTimestamp = msgOutTimestamp;
            mqttManager.publishOverTopic(cli.getTopic(), cli.getMessage());

            // If everything goes well, the method "messageReceived()" gets called and the following lines will not be executed

            // Sleep for the maximum amount of milliseconds we can wait for a message to arrive, then disconnect
            Thread.sleep(cli.getWaiting());
            mqttManager.disconnectClient();


        }
        catch (Throwable e) {
            if(loggingEnabled) {
                e.printStackTrace();
            }
            duration = -1;
//            fail();
        }
    }


    public static void main(String[] args) {

        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Tester> task = new Callable<Tester>() {
            public Tester call() {
                Tester tester = new Tester();
                tester.makeTest(args);
                return tester;
            }
        };
        Future<Tester> future = executor.submit(task);
        long duration = -1;
        try {
            Tester result = future.get(5, TimeUnit.SECONDS);
            duration = result.getDuration();
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            // handle the timeout
            fail();
        } finally {
            future.cancel(true); // may or may not desire thisù
            System.out.println(duration);
            System.exit(0);
        }
    }


    private static void fail() {
        System.out.println(-1);
        System.exit(-1);
    }
}
