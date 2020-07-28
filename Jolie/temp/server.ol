include "MosquittoInterface.iol"
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
        brokerURL = "tcp://localhost:1883",
        subscribe << {
            topic = "home/#"
        }
        // posso aggiungere tutte le options disponibili
        options << {
            setAutomaticReconnect = true
            setCleanSession = false
            setConnectionTimeout = 25
            setKeepAliveInterval = 0
            setMaxInflight = 200
        }
    }
    setMosquitto@Mosquitto (request)()
}

main {
    receive (request)
    println@Console("client ID : "+request.clientId)()
    println@Console("topic :     "+request.topic)()
    println@Console("message :   "+request.message)()
}
