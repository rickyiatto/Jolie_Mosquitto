
package org.jolielang.connector.mosquitto;

import jolie.net.CommMessage;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscribeCallback implements MqttCallback {

        private JavaService javaService;
    
        public SubscribeCallback (JavaService javaService) {
            this.javaService = javaService;
        }
    
	@Override
	public void connectionLost(Throwable cause) {
            // devo implementare un comportamento nel caso di connessione persa		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
            // metodo richiamato quando un messaggio viene consegnato
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
            // cosa succede quando un messaggio arriva --> in questo caso semplicemente lo stampo
            //System.out.println("Message arrived for the topic '" + topic + "': " + message.toString());		
            Value request = Value.create();
            request.getNewChild("topic").setValue(topic);
            request.getNewChild("message").setValue(message.toString());
            javaService.sendMessage(CommMessage.createRequest("receive", "/", request));
	}

}
