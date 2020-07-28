include "MosquittoInterface.iol"

outputPort Mosquitto {
    Interfaces: MosquittoPublisherInterface , MosquittoInterface
}

embedded {
    Java: 
        "org.jolielang.connector.mosquitto.MosquittoConnectorJavaService" in Mosquitto
}

init {
    req << {
        brokerURL = "tcp://localhost:1883"
        // posso aggiungere tutte le options disponibili
        options << {
            setAutomaticReconnect = true
            setCleanSession = false
            setConnectionTimeout = 20
            setKeepAliveInterval = 20
            setMaxInflight = 150
        }
    }
    setMosquitto@Mosquitto (req)()
}

main {
    request << {
        topic = "home/test",
        message = args[0]
    }
    sendMessage@Mosquitto (request)()
}