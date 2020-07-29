
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
            
            // implementazione delle opzioni MqttConnectOptions
            System.out.println("================================================================================");
            if (request.hasChildren("options")) {
                Value op = request.getFirstChild("options");
                if (op.hasChildren("setAutomaticReconnect")) {
                    options.setAutomaticReconnect(op.getFirstChild("setAutomaticReconnect").boolValue());
                    System.out.println("setAutomaticReconnect     correctly set   |   Value : "+op.getFirstChild("setAutomaticReconnect").boolValue());
                } else {
                    System.out.println("setAutomaticReconnect        default      |   Value : "+options.isAutomaticReconnect());
                }
                if (op.hasChildren("setCleanSession")) {
                    options.setCleanSession(op.getFirstChild("setCleanSession").boolValue());
                    System.out.println("setCleanSession           correctly set   |   Value : "+op.getFirstChild("setCleanSession").boolValue());
                } else {
                    System.out.println("setCleanSession              default      |   Value : "+options.isCleanSession());
                }
                if (op.hasChildren("setConnectionTimeout")) {
                    options.setConnectionTimeout(op.getFirstChild("setConnectionTimeout").intValue());
                    System.out.println("setConnectionTimeout      correctly set   |   Value : "+op.getFirstChild("setConnectionTimeout").intValue());
                } else {
                    System.out.println("setConnectionTimeout         default      |   Value : "+options.getConnectionTimeout());
                }
                if (op.hasChildren("setKeepAliveInterval")) {
                    options.setKeepAliveInterval(op.getFirstChild("setKeepAliveInterval").intValue());
                    System.out.println("setKeepAliveInterval      correctly set   |   Value : "+op.getFirstChild("setKeepAliveInterval").intValue());
                } else {
                    System.out.println("setKeepAliveInterval         default      |   Value : "+options.getKeepAliveInterval());
                }
                if (op.hasChildren("setMaxInflight")) {
                    options.setMaxInflight(op.getFirstChild("setMaxInflight").intValue());
                    System.out.println("setMaxInflight            correctly set   |   Value : "+op.getFirstChild("setMaxInflight").intValue());
                } else {
                    System.out.println("setMaxInflight               default      |   Value : "+options.getMaxInflight());
                }
                if (op.hasChildren("setServerURIs")) {
                    String[] serverURIs = new String[op.getChildren("setServerURIs").size()];
                    for (int i=0; i<op.getChildren("setServerURIs").size(); i++) {
                        serverURIs[i] = op.getChildren("setServerURIs").get(i).strValue();
                        System.out.println("URI["+i+"]            correctly set   |   Value : "+serverURIs[i]);
                    }
                    options.setServerURIs(serverURIs);
                }
                if (op.hasChildren("setUserName")) {
                    options.setUserName(op.getFirstChild("setUserName").strValue());
                    System.out.println("setUserName               correctly set   |   Value : "+op.getFirstChild("setUserName").strValue());
                } else {
                    System.out.println("setUserName                  default      |   Value : "+options.getUserName());
                }
                if (op.hasChildren("setPassword")) {
                    options.setPassword(op.getFirstChild("setPasswrd").strValue().toCharArray());
                    System.out.println("setPassword               correctly set   |   Value : "+op.getFirstChild("setPassword").strValue());
                } else {
                    System.out.println("setPassword                  default      |   Value : "+options.getPassword());
                }
                if (op.hasChildren("setWill")) {
                    Value setWill = op.getFirstChild("setWill");
                    String topic = setWill.getFirstChild("topicWill").strValue();
                    String payload = setWill.getFirstChild("payloadWill").strValue();
                    int qos = setWill.getFirstChild("qos").intValue();
                    boolean retained = setWill.getFirstChild("retained").boolValue();
                    options.setWill(client.getTopic(topic), payload.getBytes(), qos, retained);
                    System.out.println("setWill                   correctly set   |   Topic    : "+topic);
                    System.out.println("                                          |   Payload  : "+payload);
                    System.out.println("                                          |   Qos      : "+qos);
                    System.out.println("                                          |   Retained : "+retained);
                    
                } else {
                    System.out.println("setWill                      default      |   Value : "+options.getWillMessage());
                }
            }
            System.out.println("================================================================================");
            
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
