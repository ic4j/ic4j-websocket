package org.ic4j.websocket;

public class WebsocketError extends Error {

	public WebsocketError(Exception e) {
		super(e);
	}

	public WebsocketError(String message) {
		super(message);
	}

	public WebsocketError(String message, Exception e) {
		super(message,e);
	}

}
