include "mosquitto/interfaces/MosquittoInterface.iol"

outputPort Mosquitto {
    Interfaces: MosquittoPublisherInterface , MosquittoInterface
}

embedded {
    Java: 
        "org.jolielang.connector.mosquitto.MosquittoConnectorJavaService" in Mosquitto
}

init {
    req << {
        brokerURL = "tcp://mqtt.eclipse.org:1883"
        // I can set all the options available from the Paho library
        options << {
            setAutomaticReconnect = true
            setCleanSession = false
            setConnectionTimeout = 20
            setKeepAliveInterval = 20
            setMaxInflight = 150
            setUserName = "CLIENTadmin"
            setPassword = "password"
            setWill << {
                topicWill = "home/LWT"
                payloadWill = "client disconnected"
                qos = 0
                retained = true
            }
        }
    }
    setMosquitto@Mosquitto (req)()
}

main {
    request << {
        topic = "jolie/test",
        message = args[0]
    }
    sendMessage@Mosquitto (request)()
}