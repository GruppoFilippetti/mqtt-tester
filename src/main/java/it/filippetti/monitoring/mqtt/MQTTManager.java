package it.filippetti.monitoring.mqtt;


import it.filippetti.monitoring.listeners.MessagesListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;


public class MQTTManager implements MqttCallback {

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


	public void createClient(String serverURI, String clientName) throws MqttException {
		createClient(serverURI, clientName, this);
	}

	public void createClient(String serverURI, String clientName, MqttCallback callback) throws MqttException {
		MqttClient client = new MqttClient(serverURI, clientName, new MemoryPersistence());
		client.setCallback(callback);
		this.setClient(client);
	}

	public void connectClient() throws MqttException {
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(CLEAN_SESSION);
		options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
		connectClient(options);
	}

	public void connectClient(MqttConnectOptions options) throws MqttException {
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		this.mqttClient.connect(options);
	}


	/**
	 * @throws MqttException 
	 * 
	 */
	public void disconnectClient() throws MqttException {
		if (this.mqttClient == null) {
			throw new MqttException(0); // REASON_CODE_CLIENT_EXCEPTION
		}
		this.mqttClient.disconnect();
	}
	
	
	public void subscribeToTopic(String topic) throws MqttException {
		this.mqttClient.subscribe(topic, QOS_SUBSCRIBE);
	}
	
	public void unsubscribeFromTopic(String topic) throws MqttException {
		this.mqttClient.unsubscribe(topic);
	}
	
	public void publishOverTopic(String topic, MqttMessage message) throws MqttException {
		this.mqttClient.publish(topic, message);
	}
	
	
	public void publishOverTopic(String topic, String message) throws MqttException {
		byte[] data = message.getBytes();
		MqttMessage msg = new MqttMessage();
		msg.setPayload(data);
		this.publishOverTopic(topic, msg);
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
