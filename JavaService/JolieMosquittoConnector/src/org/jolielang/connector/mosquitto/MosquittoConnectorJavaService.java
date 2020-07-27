
package org.jolielang.connector.mosquitto;

import jolie.runtime.JavaService;
import jolie.runtime.Value;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class MosquittoConnectorJavaService extends JavaService {
    
    private MqttClient client;
    
    public Value setMosquitto (Value request) {
        String brokerURL = request.getFirstChild("brokerURL").strValue();
        String clientId;
        if (request.hasChildren("clientId")) {
            clientId = request.getFirstChild("clientId").strValue();
        } else {
            clientId = MqttClient.generateClientId();
        }
        try {
            this.client = new MqttClient(brokerURL, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false); //connessione permanente?
            options.setWill(client.getTopic("home"), "I'm gone. Bye".getBytes(), 0, false); 
            client.connect(options);
            if (request.hasChildren("subscribe")) {
                client.setCallback(new SubscribeCallback(this));
                for (int i=0; i<request.getFirstChild("subscribe").getChildren("topic").size(); i++) {
                    Value topic = request.getFirstChild("subscribe").getChildren("topic").get(i);
                    client.subscribe(topic.strValue());
                }
            }
            
	} catch (MqttException e) {
            e.printStackTrace();
	}
        return Value.create();
    }
    
    public Value sendMessage (Value request) {
        
        MqttTopic topic = client.getTopic(request.getFirstChild("topic").strValue());
		
        try {
            topic.publish(new MqttMessage(request.getFirstChild("message").strValue().getBytes()));
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        return Value.create();
    }
}
