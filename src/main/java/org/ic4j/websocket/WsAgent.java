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
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.commons.codec.digest.DigestUtils;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.ProxyBuilder;
import org.ic4j.agent.hashtree.HashTree;
import org.ic4j.agent.hashtree.Label;
import org.ic4j.agent.hashtree.LookupResult;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.replicaapi.Certificate;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.pojo.PojoDeserializer;
import org.ic4j.candid.pojo.PojoSerializer;
import org.ic4j.types.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class WsAgent {
	static Logger LOG = LoggerFactory.getLogger(WsAgent.class);
	
	static ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());
	
	static int DEFAULT_INGRESS_EXPIRY_DELTA_IN_MSECS = 5 * 60 * 1000;
	
	WebsocketTransport transport;
	Identity identity;

	Agent agent;

	Principal canisterId;
	

	boolean isLocal = false;

	Long incomingSequenceNum = 1l;
	
	Long outgoingSequenceNum = 0l;
	
	ClientKey clientKey;
	
	Principal gatewayPrincipal;
	
	boolean isConnectionEstablished = false;
	
	boolean isHandshakeCompleted = false;

	public WsAgent(Principal canisterId, WebsocketTransport transport, Identity identity) {
		super();
		
		this.agent = new AgentBuilder().transport(transport).identity(identity).build();
		this.canisterId = canisterId;
		this.transport = transport;
		this.identity = identity;
		
		this.clientKey = new ClientKey();
		
		this.clientKey.clientPrincipal = identity.sender();
		
		this.clientKey.clientNonce = new BigInteger(64,new Random());
	}

	public WsAgent setLocal(boolean isLocal) {
		this.isLocal = isLocal;
		return this;
	}


	public WsAgent addMessageHandler(Consumer<byte[]> consumer) {

		// add listener
		this.transport.addMessageHandler(new WebsocketTransport.MessageHandler() {
			public void handleMessage(byte[] bytes) throws WebsocketError {
				if(!isHandshakeCompleted)
				{
					LOG.debug("Handshake is not completed");
					
					try {
						GatewayHandshakeMessage gatewayHandshakeMessage = objectMapper.readValue(bytes, GatewayHandshakeMessage.class);
						
						isHandshakeCompleted = true;
						
						gatewayPrincipal = gatewayHandshakeMessage.gatewayPrincipal;					
						
						sendOpenMessage();
						
					}catch(Exception e)
					{
						LOG.error(e.getLocalizedMessage(), e);
					}
					
					return;
				}

				try {
					ClientIncomingMessage incomingMessage = objectMapper.readValue(bytes, ClientIncomingMessage.class);

					LOG.debug("Incoming message received. Bytes: " +  bytes.length + " bytes");
					
					try {						
						boolean isValid = isMessageBodyValid(incomingMessage.key, incomingMessage.content,
								incomingMessage.cert, incomingMessage.tree);

						if (!isValid) 
							throw new WebsocketError("Certificate validation failed");										 
						
					} catch (WebsocketError e) {
						LOG.debug(e.getLocalizedMessage(), e);
					}
					
					WebsocketMessage websocketMessage = objectMapper.readValue(incomingMessage.content, WebsocketMessage.class);

					if (websocketMessage.sequenceNum == incomingSequenceNum)
					{	
						incomingSequenceNum++;
												
						if(websocketMessage.isServiceMessage)
						{
							LOG.debug("Received service message from canister");
							handleSystemMessage(websocketMessage.content);						
						}
						else
							consumer.accept(websocketMessage.content);
					}
					else
						throw new WebsocketError("Received message sequence number does not match next expected value. Expected: " + incomingSequenceNum + ", received: "
								+ websocketMessage.sequenceNum);


				} catch (IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}

			}

		});

		return this;
	}

	public void start() {
		if (this.isLocal)
			this.agent.fetchRootKey();
		   
		/*
		this.transport.addMessageHandler(new WebsocketTransport.MessageHandler() {
			public void handleMessage(byte[] bytes) throws WebsocketError {
				if(isHandshakeCompleted)
					return;

				try {
					GatewayHandshakeMessage gatewayHandshakeMessage = objectMapper.readValue(bytes, GatewayHandshakeMessage.class);
					
					isHandshakeCompleted = true;
					
					gatewayPrincipal = gatewayHandshakeMessage.gatewayPrincipal;					
					
					sendOpenMessage();
					
				}catch(Exception e)
				{
					LOG.error(e.getLocalizedMessage(), e);
				}
			}});
			*/
		
		this.transport.open();

	}
	
	public void close()
	{
		if(this.transport != null)
			this.transport.close();
	}
	
	public void close(int code, String reason)
	{
		if(this.transport != null)
			this.transport.close(code, reason);
	}	
	
	public void sendApplicationMessage(byte[] payload) {
		this.sendApplicationMessage(payload, false);
	}
	
	void sendApplicationMessage(byte[] payload, boolean isServiceMessage) {
		  if (!this.isConnectionEstablished) 
		      throw new WebsocketError("Connection is not established yet");
		  
		WsProxy wsProxy = ProxyBuilder.create(this.agent, this.canisterId).expireAfter(Duration.ofMillis(DEFAULT_INGRESS_EXPIRY_DELTA_IN_MSECS)).getProxy(WsProxy.class);
  
		this.outgoingSequenceNum++;

		CanisterWsMessageArguments arg = new CanisterWsMessageArguments();
		
		WebsocketMessage message = new WebsocketMessage();
		
		message.clientKey = this.clientKey;
		
		message.sequenceNum = this.outgoingSequenceNum;
		
		message.timestamp = System.currentTimeMillis() * 1000000;
		
		message.content = payload;
		
		message.isServiceMessage = isServiceMessage;
		
		arg.msg = message;
		
			wsProxy.wsMessage(arg).whenComplete((input, e) -> {
				if (e == null) {
					LOG.debug("Processed application message with sequence number: " + message.sequenceNum);
				}
				else {
					LOG.debug(e.getLocalizedMessage());
					this.close(4000, "Keep alive message was not sent");
				}
			});	
			
	}
	
	public void sendOpenMessage()
	{
		WsProxy wsProxy = ProxyBuilder.create(this.agent, this.canisterId).expireAfter(Duration.ofMillis(DEFAULT_INGRESS_EXPIRY_DELTA_IN_MSECS)).getProxy(WsProxy.class);

		CanisterWsOpenArguments arg = new CanisterWsOpenArguments();

		arg.clientNonce = this.clientKey.clientNonce.longValue();
		arg.gatewayPrincipal = this.gatewayPrincipal;

		try {
			wsProxy.wsOpen(arg).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new WebsocketError(e.getLocalizedMessage(),e);
		}
		
	}
	
	void sendKeepAliveMessage(){
		ClientKeepAliveMessageContent keepAliveMessageContent  = new ClientKeepAliveMessageContent();
		  
		keepAliveMessageContent.lastIncomingSequenceNum = this.incomingSequenceNum - 1l;
		  
		try
		{
			byte[] payload = this.encodeValue(keepAliveMessageContent);
			
			LOG.debug("Sending keep alive message for client " + this.clientKey.clientPrincipal.toString() + " nonce " + this.clientKey.clientNonce);
			  
			this.sendApplicationMessage(payload, true); 		
		}
		catch(Exception e)
		{
			throw new WebsocketError(e.getLocalizedMessage(),e);
		}    

	  }	


	boolean handleSystemMessage(byte[] serviceMessageContent)
	{
		try
		{
			WebsocketServiceMessageContent result = IDLArgs.fromBytes(serviceMessageContent).getArgs().get(0).getValue(new PojoDeserializer(), WebsocketServiceMessageContent.class);
		
			switch(result)
			{
				case OpenMessage:
					LOG.debug("Received open message from canister");
					if(this.clientKey.equals(result.openValue.clientKey))
						throw new WebsocketError("Client key does not match");	
					
					this.isConnectionEstablished = true;
					break;
				case AckMessage:
					this.handleAckMessageFromCanister(result.ackValue);
					break;
				case KeepAliveMessage:	
				break;
				default:
					throw new WebsocketError("Invalid service message from canister");	
			}
		}catch(Exception e)
		{
			LOG.error(e.getLocalizedMessage(), e);
			this.transport.close(4000, "Service message error");
			return false;
		}
		return true;
	}
	
	void handleAckMessageFromCanister(CanisterAckMessageContent content) {
	    Long lastAckSequenceNumberFromCanister = content.lastIncomingSequenceNum;
	    LOG.debug("Received ack message from canister with sequence number", lastAckSequenceNumberFromCanister);

	    
	    try {
	    	this.sendKeepAliveMessage();	    	
	    }
		catch(Exception e)
		{
			throw new WebsocketError(e);
		}
	    
	  }


	
	public static byte[] encodeValue(Object value)
	{
		IDLValue idlValue = IDLValue.create(value, new PojoSerializer());

		List<IDLValue>  args = new ArrayList<IDLValue>();
		args.add(idlValue);

		IDLArgs idlArgs = IDLArgs.create(args);

		byte[] buf = idlArgs.toBytes();
		
		return buf;
	}
	
	boolean isWebsocketMessageSequenceNumberValid(WebsocketMessage incomingContent) {
	    long receivedNum = incomingContent.sequenceNum;
	    LOG.debug("Received message with sequence number", receivedNum);
	    return receivedNum == this.incomingSequenceNum;
	  }

	void _inspectWebsocketMessageTimestamp(WebsocketMessage incomingContent) {
	    Long time = incomingContent.timestamp / (1000000);
	    Long delayMilliseconds = System.currentTimeMillis() - time;
	    LOG.debug("Canister --> client latency(ms):", delayMilliseconds);
	  }	

	boolean isMessageBodyValid(String path, byte[] body, byte[] certificate, byte[] tree) {
		ObjectMapper objectMapper = new ObjectMapper(new CBORFactory());
		objectMapper.registerModule(new Jdk8Module());

		try {
			Certificate cert = objectMapper.readValue(certificate, Certificate.class);

			HashTree hashTree = objectMapper.readValue(tree, HashTree.class);

			List<Label> pathCanister = new ArrayList<Label>();
			pathCanister.add(new Label("canister"));
			pathCanister.add(new Label(this.canisterId.getValue()));
			pathCanister.add(new Label("certified_data"));

			LookupResult result = cert.tree.lookupPath(pathCanister);

			if (result.status != LookupResult.LookupResultStatus.FOUND)
				throw new WebsocketError("Could not find certified data for this canister in the certificate.");

			if (!Arrays.equals(hashTree.digest(), result.value))
				throw new WebsocketError("[certification] Witness != Tree passed in ic-certification");

			List<Label> pathWebsocket = new ArrayList<Label>();
			pathWebsocket.add(new Label("websocket"));
			pathWebsocket.add(new Label(path));

			result = hashTree.lookupPath(pathWebsocket);

			if (result.status != LookupResult.LookupResultStatus.FOUND)
				throw new WebsocketError("[certification] Invalid Tree in the header. Does not contain path " + path);

			byte[] sha = DigestUtils.sha256(body);

			return Arrays.equals(sha, result.value);

		} catch (Exception e) {
			throw new WebsocketError(e);
		}

	}

}
