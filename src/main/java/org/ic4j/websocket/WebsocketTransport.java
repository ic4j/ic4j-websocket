package org.ic4j.websocket;

public interface WebsocketTransport {
	
	public void addMessageHandler(MessageHandler msgHandler);
	public void sendMessage(byte[] envelope);
	
	/**
	 * Message handler.
	 *
	 */
	public static interface MessageHandler 
	{
	       public void handleMessage(byte[] envelope) throws WebsocketError;
	}
}
