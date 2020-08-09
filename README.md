# Jolie_Mosquitto

Jolie connector for Mosquitto framework

- Jolie: https://www.jolie-lang.org/
- Mosquitto: https://mosquitto.org/

## Architecture

The MQTT protocol is based on the principle of publishing messages and subscribing to topics, or "pub/sub". Multiple clients connect to a broker and subscribe to topics that they are interested in. Clients also connect to the broker and publish messages to topics. Many clients may subscribe to the same topics and do with the information as they please. The broker and MQTT act as a simple, common interface for everything to connect to.

![Architecture](https://github.com/rickyiatto/Jolie_Mosquitto/blob/develop/architecture.png)

JavaService uses the Paho library to create both the publisher and the subscriber.
Jolie's client service communicates with the JavaService, which takes care of creating a publisher client, creating the connection with the Mosquitto broker and finally transmitting the message.
Jolie's server service communicates with JavaService, which is responsible for creating a subscriber client, creating the connection with the Mosquitto broker and finally subscribing to the desired topics.

To install the Mosquitto broker on your computer follow the instructions provided by the official Eclipse Mosquitto website at the link: https://mosquitto.org/download/

## Example

### client_server :

To be able to use the connector correctly, be sure to add both the file JolieMosquittoConnector.jar and org.eclipse.paho.client.mqttv3-1.1.2-20170804.042534-34.jar to your project folder.
- ```JolieMosquittoConnector.jar``` contains both the connector and the interfaces necessary for the Jolie service to communicate with the Mosquitto broker.
- ```org.eclipse.paho.client.mqttv3-1.1.2-20170804.042534-34.jar``` is the dependency on the Paho library that JavaService uses to create the publisher and subscriber.

#### server.ol

```java
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
        brokerURL = "tcp://localhost:1883",
        subscribe << {
            topic = "home/#"
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
                retained = false
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
```

You can modify all options values and the topic you want to subscribe in. The string "home/#" is used to subscribe on every subtopic of "home".
An example of launch of this client is:  
    ```jolie server.ol```.

The interface to be included must follow exactly the path reported ```"mosquitto/interfaces/MosquittoInterface.iol"```, as the requested file is located in the jar at this address.

#### client.ol

```java
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
        brokerURL = "tcp://localhost:1883"
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
                retained = false
            }
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
```

You can modify all options values and the topic you want to publish in.
An example of launch of this client is:  
    ```jolie client.ol "hello"```.

The interface to be included must follow exactly the path reported ```"mosquitto/interfaces/MosquittoInterface.iol"```, as the requested file is located in the jar at this address.

#### MosquittoInterface.iol

```java
type SetMosquittoRequest: void {
    clientId?: string
    brokerURL: string
    options?: void {
        setAutomaticReconnect?: bool
        setCleanSession?: bool
        setConnectionTimeout?: int
        setKeepAliveInterval?: int
        setMaxInflight?: int
        setServerURIs?: string
    }
    subscribe?: void {
        topic[1,*]: string
    }
}

type MosquittoMessageRequest: void {
    topic: string
    message: string
}

interface MosquittoPublisherInterface {
    RequestResponse: 
        sendMessage (MosquittoMessageRequest)(void)
}

interface MosquittoInterface {
    RequestResponse:
        setMosquitto (SetMosquittoRequest)(void),
}

interface MosquittoReceiverInteface {
    OneWay: 
        receive (MosquittoMessageRequest)
}
```

The ```MosquittoPublisherInterface``` exposes a method called ```sendMessage``` which receives an input request of the ```MosquittoMessageRequest``` type. This type requires two fields: a ```topic``` (to publish the message) and a ```message``` (to publish).

The ```MosquittoInterface``` exposes a method called ```setMosquitto``` which receives in input a request of the type ```SetMosquittoRequest```. This type requires two mandatory fields: a ```brokerURL``` (which is used to connect to the broker Mosquitto) and a ```clientId``` (if not specified a random one is generated).
You can also specify some values of the ```options``` to customize the connection (if not specified, the default values are used).

The ```MosquittoReceiverInteface``` exposes a method called ```receive``` which receives a ```MosquittoMessageRequest``` request, described above.

This interface is already inside the ```JolieMosquittoConnector.jar``` file.
To be included correctly you need to call it through the string ```include "mosquitto/interfaces/MosquittoInterface.iol"```.

### chat :

In this example I wanted to apply the MQTT communication protocol to a simple chat.
Always exploiting the JavaService explained in the previous example, and exploiting Leonardo (a Web Server written in Jolie: https://github.com/jolie/leonardo) is sufficient to launch the command ```jolie leonardo.ol``` and subsequently open a browser to the page ```localhost:16000``` to observe its operation.

#### frontend.ol

```java
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
        options.debug = true
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
```

The ```frontend.ol``` service presents an ```outputPort``` in which the JavaService described in the previous example is embedded, while the ```inputPort``` communicates both with the JavaService and with the ```FrontendInterface``` interface.
In the ```init``` method, it prepares the request (setting all the desired parameters) to send to the JavaService to open a connection with the broker Mosquitto.
In the ```main``` method, instead, it develops four operations:
- **setUsername:** sets the username of the user who wants to connect to the chat.
- **sendChatMessage:** publish the messages sent in the chat at the broker Mosquitto to the topic set in the connection. The message is converted into a json to allow the sending of additional information along with the message text.
- **receive:** receives the messages from broker Mosquitto and saves them in a global variable ```messageQueue```.
- **getChatMessages:** this operation reads all the messages in the queue, sends them to the WebService which prints them in the chat and empties the ```messageQueueue```. This operation is called cyclically every 0.5 seconds by the web page.

## Options

In order to let you customize your communications, you can modify some options (these descriptions are taken from the official Paho library documentation):
https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html

- **setAutomaticReconnect : bool**
Sets whether the client will automatically attempt to reconnect to the server if the connection is lost.
    - If set to **false**, the client will not attempt to automatically reconnect to the server in the event that the connection is lost.
    - If set to **true**, in the event that the connection is lost, the client will attempt to reconnect to the server. 
    It will initially wait 1 second before it attempts to reconnect, for every failed reconnect attempt, the delay will double until it is at 2 minutes at which point the delay will stay at 2 minutes.

- **setCleanSession : bool**
Sets whether the client and server should remember state across restarts and reconnects.
    - If set to **false** both the client and server will maintain state across restarts of the client, the server and the connection. As state is maintained:
        - Message delivery will be reliable meeting the specified QOS even if the client, server or connection are restarted.
        - The server will treat a subscription as durable.
    - If set to **true** the client and server will not maintain state across restarts of the client, the server or the connection. This means:
        - Message delivery to the specified QOS cannot be maintained if the client, server or connection are restarted
        - The server will treat a subscription as non-durable.

- **setConnectionTimeout : int**
Sets the connection timeout value. This value, measured in seconds, defines the maximum time interval the client will wait for the network connection to the MQTT server to be established. The default timeout is 30 seconds. 
A value of 0 disables timeout processing meaning the client will wait until the network connection is made successfully or fails.

- **setKeepAliveInterval : int**
Sets the "keep alive" interval. This value, measured in seconds, defines the maximum time interval between messages sent or received. It enables the client to detect if the server is no longer available, without having to wait for the TCP/IP timeout. The client will ensure that at least one message travels across the network within each keep alive period. In the absence of a data-related message during the time period, the client sends a very small "ping" message, which the server will acknowledge. A value of 0 disables keepalive processing in the client.
The default value is 60 seconds.

- **setMaxInflight : int**
Sets the "max inflight". please increase this value in a high traffic environment.
The default value is 10.

- **setUserName : string**
Sets the user name to use for the connection.

- **setPassword : char[]**
Sets the password to use for the connection.

- **setServerURIs : string[]**
Set a list of one or more serverURIs the client may connect to.
Each serverURI specifies the address of a server that the client may connect to. Two types of connection are supported tcp:// for a TCP connection and ssl:// for a TCP connection secured by SSL/TLS. For example:
    tcp://localhost:1883
    ssl://localhost:8883
If the port is not specified, it will default to 1883 for tcp://" URIs, and 8883 for ssl:// URIs.
If serverURIs is set then it overrides the serverURI parameter passed in on the constructor of the MQTT client.
When an attempt to connect is initiated the client will start with the first serverURI in the list and work through the list until a connection is established with a server. If a connection cannot be made to any of the servers then the connect attempt fails.
Specifying a list of servers that a client may connect to has several uses:
    - **High Availability and reliable message delivery**
    Some MQTT servers support a high availability feature where two or more "equal" MQTT servers share state. An MQTT client can connect to any of the "equal" servers and be assured that messages are reliably delivered and durable subscriptions are maintained no matter which server the client connects to.
    The cleansession flag must be set to false if durable subscriptions and/or reliable message delivery is required.
    - **Hunt List**
    A set of servers may be specified that are not "equal" (as in the high availability option). As no state is shared across the servers reliable message delivery and durable subscriptions are not valid. The cleansession flag must be set to true if the hunt list mode is used.

- **setWill : void**
Sets the "Last Will and Testament" (LWT) for the connection. In the event that this client unexpectedly loses its connection to the server, the server will publish a message to itself using the supplied details.
**Parameters**:
    - **topic** - the topic to publish to : ```string```
    - **payload** - the byte payload for the message : ```string```
    - **qos** - the quality of service to publish the message at (0, 1 or 2) : ```int```
    - **retained** - whether or not the message should be retained : ```bool```