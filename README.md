# Jolie_Mosquitto

Jolie connector for Mosquitto framework

- Jolie: https://www.jolie-lang.org/
- Mosquitto: https://mosquitto.org/

## Architecture

The MQTT protocol is based on the principle of publishing messages and subscribing to topics, or "pub/sub". Multiple clients connect to a broker and subscribe to topics that they are interested in. Clients also connect to the broker and publish messages to topics. Many clients may subscribe to the same topics and do with the information as they please. The broker and MQTT act as a simple, common interface for everything to connect to.

![Architecture](\architecture.png)

In this case, the Jolie client is the publisher and publish the message passed as argument from command line.
The Jolie server is the subscriber. It can subscribe to one or more topics and receives all message published on that topics.

## Example

#### client.ol

```java
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
```

You can modify all options values and the topic you want to publish in.
An example of launch of this client is:  
    jolie client.ol "hello".

#### server.ol

```java
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
```

You can modify all options values and the topic you want to subscribe in. The string "home/#" is used to subscribe on every subtopic of "home".
An example of launch of this client is:  
    jolie server.ol.

## Options

In order to let you customize your communications, you can modify some options:

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
