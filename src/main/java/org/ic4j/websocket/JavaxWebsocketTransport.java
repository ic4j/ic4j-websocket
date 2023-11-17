/*
 * Copyright 2023 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ic4j.websocket;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.ic4j.agent.ReplicaTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@ClientEndpoint
public class JavaxWebsocketTransport extends WebsocketTransport {
	ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());
	Session userSession = null;

	public JavaxWebsocketTransport(URI endpointURI, ReplicaTransport httpTransport) {
		super(endpointURI, httpTransport);
	}

	static Logger LOG = LoggerFactory.getLogger(JavaxWebsocketTransport.class);

	/**
	 * Callback hook for Connection open events.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		LOG.debug("opening websocket");
		this.userSession = userSession;
	}

	/**
	 * Callback hook for Connection close events.
	 *
	 * @param userSession the userSession which is getting closed.
	 * @param reason      the reason for connection close
	 */
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		LOG.debug("closing websocket");
		this.userSession = null;
	}

	/**
	 * Callback hook for Message Events. This method will be invoked when a client
	 * send a message.
	 *
	 * @param message The text message
	 */
	@OnMessage
	public void onMessage(String message) {
		if (this.messageHandler != null) {
			if (message != null)
				try {
					this.messageHandler.handleMessage(message.getBytes());
				} catch (WebsocketError e) {
					LOG.error(e.getLocalizedMessage(), e);
					try {
						userSession.close(
								new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getLocalizedMessage()));
					} catch (IOException e1) {
						LOG.error(message, e1);
					}
				}
		}
	}

	/**
	 * Callback hook for Message Events. This method will be invoked when a client
	 * send a message.
	 *
	 * @param bytes Byte buffer
	 */

	@OnMessage
	public void onMessage(ByteBuffer bytes) {
		if (this.messageHandler != null) {
			try {
				this.messageHandler.handleMessage(bytes.array());
			} catch (WebsocketError e) {
				LOG.error(e.getLocalizedMessage(), e);
				try {
					userSession.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.getLocalizedMessage()));
				} catch (IOException e1) {
					LOG.error(e1.getLocalizedMessage(), e1);
				}
			}
		}
	}

	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	public void addMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}


	void send(ByteBuffer message) {
		this.userSession.getAsyncRemote().sendBinary(message);
	}

	@Override
	public void open() {
		try {

			WebSocketContainer container = ContainerProvider.getWebSocketContainer();

			container.setDefaultMaxSessionIdleTimeout(0);

			container.connectToServer(this, this.endpointURI);
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new WebsocketError(e);
		}

	}
	
	public void setMaxIdleTimeout(int timeout)
	{
		if(this.userSession != null)
			this.userSession.setMaxIdleTimeout(timeout);
	}

	@Override
	public void close() {
		if (this.userSession != null)
			try {
				this.userSession.close();
			} catch (IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
	}

	public void close(int code, String reason) {
		if (this.userSession != null)
			try {
				CloseCode closeCode = CloseReason.CloseCodes.getCloseCode(code);
				CloseReason closeReason = new CloseReason(closeCode, reason);
				this.userSession.close(closeReason);
			} catch (IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
	}

}
