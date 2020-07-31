include "mosquitto/interfaces/MosquittoInterface.iol"
include "console.iol"

execution {concurrent}

inputPort Server {
    Location: "local"
    Protocol: sodep
    Interfaces: MosquittoReceiverInteface
}

outputPort Mosquitto {
    Interfaces: MosquittoInterface
}

embedded {
    Java: 
        "org.jolielang.connector.mosquitto.MosquittoConnectorJavaService" in Mosquitto
}

init {
    request << {
        brokerURL = "tcp://mqtt.eclipse.org:1883",
        subscribe << {
            topic = "jolie/#"
        }
        // I can set all the options available from the Paho library
        options << {
            setAutomaticReconnect = true
            setCleanSession = false
            setConnectionTimeout = 25
            setKeepAliveInterval = 0
            setMaxInflight = 200
            setUserName = "SERVERadmin"
            setPassword = "passwordAdmin"
            setWill << {
                topicWill = "home/LWT"
                payloadWill = "server disconnected"
                qos = 0
                retained = true
            }
        }
    }
    setMosquitto@Mosquitto (request)()
}

main {
    receive (request)
    println@Console("topic :     "+request.topic)()
    println@Console("message :   "+request.message)()
}
