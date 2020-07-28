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
        // setSocketFactory
        // setSLLProprieties
        // setMqttVersion  
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