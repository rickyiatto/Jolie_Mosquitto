include "FrontendInterface.iol"
include "mosquitto/interfaces/MosquittoInterface.iol"
include "console.iol"
include "json_utils.iol"

execution {concurrent}

outputPort Mosquitto {
    Interfaces: MosquittoPublisherInterface , MosquittoInterface
}

embedded {
    Java: 
        "org.jolielang.connector.mosquitto.MosquittoConnectorJavaService" in Mosquitto
}

inputPort Frontend {
    Location: "local"
    Protocol: sodep
    Interfaces: MosquittoReceiverInteface, FrontendInterface
}

init {
    
    request << {
        brokerURL = "tcp://mqtt.eclipse.org:1883",
        subscribe << {
            topic = "jolie/test/chat"
        }
        // I can set all the options available from the Paho library
        options = void
    }
    setMosquitto@Mosquitto (request)()
    println@Console("SUBSCRIBER connection done! Waiting for message on topic : "+request.subscribe.topic)()
    
}

main {

    [ receive (request) ]
    {
        getJsonValue@JsonUtils(request.message)(jsonMessage)
        global.messageQueue[#global.messageQueue] << {
            message = jsonMessage.message
            username = jsonMessage.username
        }
    }

    [ getChatMessages( GetChatMessagesRequest )( GetChatMessagesResponse ) {
        for (i=0, i<#global.messageQueue, i++) {
            GetChatMessagesResponse.messageQueue[i] << global.messageQueue[i]
        }
        undef(global.messageQueue)
    }]

    [ sendChatMessage( messageRequest )( response ) {
        json << {
            username = global.username
            message = messageRequest.message
        }
        getJsonString@JsonUtils(json)(jsonString)
        req << {
            topic = "jolie/test/chat",
            message = jsonString
        }
        println@Console("PUBLISHER ["+global.username+"] connection done! Message correctly send: "+messageRequest.message)()
        sendMessage@Mosquitto (req)()
	}]

    [ setUsername( usernameRequest )( usernameResponse ) {
        global.username = usernameRequest.username
        println@Console("Username set for the current session: "+global.username)()
    }]
}