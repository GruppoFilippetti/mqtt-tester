package it.filippetti.monitoring.mqtt;


import it.filippetti.monitoring.listeners.MessagesListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MQTTManager implements MqttCallback {

	private static final Logger logger = Logger.getLogger(MQTTManager.class.getName());
	
	private MqttClient mqttClient;
	private static final int QOS_SUBSCRIBE = 0;
	private static final boolean CLEAN_SESSION = true;
	private static final int KEEP_ALIVE_INTERVAL = 30;

	private List<MessagesListener> listeners = new ArrayList<MessagesListener>();


	public MQTTManager() {}

	public MqttClient getMQTTClient() {
		return this.mqttClient;
	}

	public void addListener(MessagesListener listener) {
		listeners.add(listener);
	}
	
	public void setClient(MqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}


	public boolean createClient(String serverURI, String clientName) {
		return createClient(serverURI, clientName, this);
	}

	public boolean createClient(String serverURI, String clientName, MqttCallback callback) {
		logger.log(Level.INFO, "Creating MQTT client... ");
		MqttClient client = null;
		try {
			client = new MqttClient(serverURI, clientName, new MemoryPersistence());
			logger.log(Level.INFO, "Client was created.");
			client.setCallback(callback);
			logger.log(Level.INFO, "Callback function assigned to MQTT client.");
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while creating client: " + e);
			return false;
		}
		this.setClient(client);
		return true;
	}
	
	/**
	 * @throws MqttException 
	 * 
	 */
	public boolean connectClient() throws MqttException {
		logger.log(Level.INFO, "Connecting...");
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(CLEAN_SESSION);
		options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
		return connectClient(options);
	}

	public boolean connectClient(MqttConnectOptions options) throws MqttException {
		logger.log(Level.INFO, "Connecting...");
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		try {
			this.mqttClient.connect(options);
			logger.log(Level.INFO, "Connected.");
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while connecting: " + e);
			return false;
		}
		return true;
	}


	/**
	 * @throws MqttException 
	 * 
	 */
	public boolean disconnectClient() throws MqttException {
		logger.log(Level.INFO, "Disconnecting...");
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		try {
			this.mqttClient.disconnect();
			logger.log(Level.INFO, "Disconnected.");
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while disconnecting: " + e);
			return false;
		}
		return true;
	}
	
	
	public boolean subscribeToTopic(String topic) {
		logger.log(Level.INFO, "Subscribing to topic " + topic + "... ");
		try {
			this.mqttClient.subscribe(topic, QOS_SUBSCRIBE);
			logger.log(Level.INFO, "Subscribed.");
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while subscribing to topic: " + e);
			return false;
		}
		return true;
	}
	
	public boolean unsubscribeFromTopic(String topic) {
		logger.log(Level.INFO, "Unsubscribing from topic " + topic + "... ");
		try {
			this.mqttClient.unsubscribe(topic);
			logger.log(Level.INFO, "Unsubscribed.");
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while unsubscribing from topic: " + e);
			return false;
		}
		return true;
	}
	
	public boolean publishOverTopic(String topic, MqttMessage message) {
		logger.log(Level.INFO, "Publishing over topic " + topic + "... ");
		try {
			this.mqttClient.publish(topic, message);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, "MQTT error while publishing over topic: " + e);
			return false;
		}
		return true;
	}
	
	
	public boolean publishOverTopic(String topic, String message) {
		byte[] data = message.getBytes(); 
		MqttMessage msg = new MqttMessage();
		msg.setPayload(data);
		return this.publishOverTopic(topic, msg);
	}

	@Override
	public void connectionLost(Throwable throwable) {
		//System.out.println("CONNECTION LOST-------------------------------------------------");
	}

	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		long msgInTimestamp = System.currentTimeMillis();
		for (MessagesListener l : listeners)
			l.messageReceived(msgInTimestamp);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		//long msgOutTimestamp = System.currentTimeMillis();
		//lastPublishedMessageTimestamp = msgOutTimestamp;
	}

}
