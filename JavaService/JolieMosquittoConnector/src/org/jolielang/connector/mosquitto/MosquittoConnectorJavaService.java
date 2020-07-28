
package org.jolielang.connector.mosquitto;

import jolie.runtime.JavaService;
import jolie.runtime.Value;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

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
            this.client = new MqttClient(brokerURL, clientId, new MqttDefaultFilePersistence("/tmp"));
            MqttConnectOptions options = new MqttConnectOptions();
            // prova da qui
            
            if (request.hasChildren("options")) {
                Value op = request.getFirstChild("options");
                if (op.hasChildren("setAutomaticReconnect")) {
                    options.setAutomaticReconnect(op.getFirstChild("setAutomaticReconnect").boolValue());
                    System.out.println("setAutomaticReconnect impostato correttamente!   valore : "+op.getFirstChild("setAutomaticReconnect").boolValue());
                }
                if (op.hasChildren("setCleanSession")) {
                    options.setCleanSession(op.getFirstChild("setCleanSession").boolValue());
                    System.out.println("setCleanSession impostato correttamente!   valore : "+op.getFirstChild("setCleanSession").boolValue());
                }
                if (op.hasChildren("setConnectionTimeout")) {
                    options.setConnectionTimeout(op.getFirstChild("setConnectionTimeout").intValue());
                    System.out.println("setConnectionTimeout impostato correttamente!   valore : "+op.getFirstChild("setConnectionTimeout").intValue());
                }
                if (op.hasChildren("setKeepAliveInterval")) {
                    options.setKeepAliveInterval(op.getFirstChild("setKeepAliveInterval").intValue());
                    System.out.println("setKeepAliveInterval impostato correttamente!   valore : "+op.getFirstChild("setKeepAliveInterval").intValue());
                }
                if (op.hasChildren("setMaxInflight")) {
                    options.setMaxInflight(op.getFirstChild("setMaxInflight").intValue());
                    System.out.println("setMaxInflight impostato correttamente!   valore : "+op.getFirstChild("setMaxInflight").intValue());
                }
                if (op.hasChildren("setServerURIs")) {
                    String[] serverURIs = new String[op.getChildren("setServerURIs").size()];
                    for (int i=0; i<op.getChildren("setServerURIs").size(); i++) {
                        serverURIs[i] = op.getChildren("setServerURIs").get(i).strValue();
                        System.out.println("URI["+i+"] impostata : "+serverURIs[i]);
                    }
                    options.setServerURIs(serverURIs);
                }
            }
            
            // a qui
            
            //options.setCleanSession(false);
            //options.setWill(client.getTopic("home/LWT"), "I'm gone. Bye".getBytes(), 0, false); 
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
