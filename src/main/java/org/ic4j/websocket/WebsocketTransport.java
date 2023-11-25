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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.ArrayUtils;
import org.ic4j.agent.ReplicaResponse;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.replicaapi.Envelope;
import org.ic4j.agent.replicaapi.CallRequestContent.CallRequest;
import org.ic4j.agent.requestid.RequestId;
import org.ic4j.types.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public abstract class WebsocketTransport implements ReplicaTransport {
	static Logger LOG = LoggerFactory.getLogger(WebsocketTransport.class);
	
	ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());

	protected MessageHandler messageHandler;

	protected URI endpointURI;

	ReplicaTransport httpTransport;

	public WebsocketTransport(URI endpointURI, ReplicaTransport httpTransport) {
		this.endpointURI = endpointURI;
		this.httpTransport = httpTransport;
	}

	public abstract void open();

	public abstract void addMessageHandler(MessageHandler msgHandler);

	public abstract void close(int code, String reason);
	
	public abstract void setMaxIdleTimeout(int timeout);
	
	abstract void send(ByteBuffer message);
	
	/**
	 * Send a message.
	 *
	 * @param payload Byte array
	 */
	protected void send(byte[] payload) {
		WsAgentRequestMessage<CallRequest> message = new WsAgentRequestMessage<CallRequest>();

		try {
			Envelope<CallRequest> envelope = objectMapper.readValue(payload, Envelope.class);
			message.envelope = envelope;

			this.sendMessage(message);
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new WebsocketError(e);
		}
	}	

	public void sendMessage(Object message) {		
		ObjectWriter objectWriter = objectMapper.writerFor(message.getClass());
		try {
			byte[] buf = objectWriter.writeValueAsBytes(message);

			ByteBuffer bytes = ByteBuffer.wrap(buf);

			this.send(bytes);

		} catch (JsonProcessingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public CompletableFuture<ReplicaResponse> status() {
		return this.httpTransport.status();
	}

	@Override
	public CompletableFuture<ReplicaResponse> query(Principal canisterId, byte[] envelope,
			Map<String, String> headers) {
		return this.httpTransport.query(canisterId, envelope, headers);
	}

	@Override
	public CompletableFuture<ReplicaResponse> call(Principal canisterId, byte[] payload, RequestId requestId,
			Map<String, String> headers) {

		CompletableFuture<ReplicaResponse> response = new CompletableFuture<ReplicaResponse>();
		try {
			this.send(payload);

			ReplicaResponse replicaResponse = new ReplicaResponse();

			replicaResponse.payload = ArrayUtils.EMPTY_BYTE_ARRAY;

			response.complete(replicaResponse);
		} catch (Exception e) {
			response.completeExceptionally(e);
		}

		return response;
	}

	@Override
	public CompletableFuture<ReplicaResponse> readState(Principal canisterId, byte[] envelope,
			Map<String, String> headers) {
		return null;
	}

	/**
	 * Message handler.
	 *
	 */
	public static interface MessageHandler {
		public void handleMessage(byte[] envelope) throws WebsocketError;
	}
}
