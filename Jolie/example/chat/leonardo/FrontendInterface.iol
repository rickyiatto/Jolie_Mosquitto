type SendChatMessageRequest: void {
    message: string
} 

type GetChatMessagesResponse: void {
    messageQueue*: void {
        message: string
        username: string
    }
}

type SetUsernameRequest: void {
    username: string
}

interface FrontendInterface {
    RequestResponse:
		sendChatMessage( SendChatMessageRequest )( void ),
        getChatMessages( void )( GetChatMessagesResponse ),
        setUsername( SetUsernameRequest )( void )
}