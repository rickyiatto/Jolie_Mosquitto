type SetMosquittoRequest: void {
    clientId?: string
    brokerURL: string
    options?: void {
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