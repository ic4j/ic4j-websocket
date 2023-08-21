package org.ic4j.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.commons.codec.digest.DigestUtils;
import org.ic4j.agent.Agent;
import org.ic4j.agent.ProxyBuilder;
import org.ic4j.agent.hashtree.HashTree;
import org.ic4j.agent.hashtree.Label;
import org.ic4j.agent.hashtree.LookupResult;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.identity.Signature;
import org.ic4j.agent.replicaapi.Certificate;
import org.ic4j.types.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class WebsocketClient {
	static Logger LOG = LoggerFactory.getLogger(WebsocketClient.class);
	WebsocketTransport transport;
	Identity identity;

	Agent agent;

	Principal canisterId;

	byte[] senderKey;

	boolean isLocal = false;

	int sequenceNum = 1;

	public WebsocketClient(Agent agent, Principal canisterId, WebsocketTransport transport, Identity identity,
			byte[] senderKey) {
		super();
		this.agent = agent;
		this.canisterId = canisterId;
		this.transport = transport;
		this.identity = identity;
		this.senderKey = senderKey;
	}

	public WebsocketClient setLocal(boolean isLocal) {
		this.isLocal = isLocal;
		return this;
	}

	public WebsocketClient addMessageHandler(Consumer<byte[]> consumer) {
		ObjectMapper objectMapper = new ObjectMapper(new CBORFactory());
		objectMapper.registerModule(new Jdk8Module());

		// add listener
		transport.addMessageHandler(new WebsocketTransport.MessageHandler() {
			public void handleMessage(byte[] bytes) throws WebsocketError {

				try {
					ClientIncomingMessage incomingMessage = objectMapper.readValue(bytes, ClientIncomingMessage.class);

					try {
						boolean isValid = isMessageBodyValid(incomingMessage.key, incomingMessage.val,
								incomingMessage.cert, incomingMessage.tree);

						if (!isValid) {

						}
					} catch (WebsocketError e) {
						LOG.debug(e.getLocalizedMessage(), e);
					}

					ClientIncomingMessageContent incomingMessageContent = objectMapper.readValue(incomingMessage.val,
							ClientIncomingMessageContent.class);

					if (incomingMessageContent.sequenceNum == sequenceNum)
						consumer.accept(incomingMessageContent.message);
					else
						throw new WebsocketError("Received message sequence number does not match next expected value "
								+ incomingMessageContent.sequenceNum);

					sequenceNum++;
				} catch (IOException e) {
					throw new WebsocketError(e);
				}

			}

		});

		return this;
	}

	public void start() {
		if (this.isLocal)
			this.agent.fetchRootKey();

		WSProxy wsProxy = ProxyBuilder.create(this.agent, this.canisterId).getProxy(WSProxy.class);

		CanisterWsRegisterArguments arg = new CanisterWsRegisterArguments();

		arg.clientKey = this.senderKey;

		CanisterWsRegisterResult result;
		try {
			result = wsProxy.wsRegister(arg).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new WebsocketError(e.getLocalizedMessage(),e);
		}
		
		if(result == CanisterWsRegisterResult.Err)
			throw new WebsocketError(result.errValue);

		CanisterFirstMessageContent firstMessageContent = new CanisterFirstMessageContent();

		firstMessageContent.clientKey = this.senderKey;
		firstMessageContent.canisterId = this.canisterId.toString();

		this.sendFirstMessage(firstMessageContent);

	}
	
	public void sendApplicationMessage(byte[] payload) {

		WSProxy wsProxy = ProxyBuilder.create(this.agent, this.canisterId).getProxy(WSProxy.class);

		CanisterWsMessageArguments arg = new CanisterWsMessageArguments();
		
		CanisterIncomingMessage message = CanisterIncomingMessage.DirectlyFromClient;
		
		
		DirectClientMessage content = new DirectClientMessage();
		
		content.clientKey = this.senderKey;
		content.message = payload;
		
		message.directlyFromClientValue = content;
		
		arg.msg = message;
		
		try {
			CanisterWsMessageResult result = wsProxy.wsMessage(arg).get();
			
			if(result == CanisterWsMessageResult.Err)
				throw new WebsocketError(result.errValue);
			
		} catch (InterruptedException | ExecutionException e) {
			throw new WebsocketError(e);
		}
	}	

	void sendFirstMessage(CanisterFirstMessageContent content) {
		if(this.transport == null)
			throw new WebsocketError("Connection is not open");

		ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());

		ObjectWriter objectWriter = objectMapper.writerFor(CanisterFirstMessageContent.class);

		RelayedClientMessage<CanisterFirstMessageContent> message = new RelayedClientMessage<CanisterFirstMessageContent>();

		byte[] buf = null;
		try {
			buf = objectWriter.writeValueAsBytes(content);

		} catch (JsonProcessingException e) {
			throw new WebsocketError(e);
		}

		Signature signature = identity.sign(buf);

		message.sig = signature.signature.get();
		message.content = buf;

		objectWriter = objectMapper.writerFor(RelayedClientMessage.class);

		buf = null;
		try {
			buf = objectWriter.writeValueAsBytes(message);

			transport.sendMessage(buf);
		} catch (JsonProcessingException e) {
			throw new WebsocketError(e);
		}
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
