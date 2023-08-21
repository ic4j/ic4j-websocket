package org.ic4j.websocket.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaOkHttpTransport;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.types.Principal;

import org.ic4j.websocket.JavaxWebsocketTransport;
import org.ic4j.websocket.Utils;
import org.ic4j.websocket.WebsocketClient;
import org.ic4j.websocket.WebsocketError;
import org.ic4j.websocket.WebsocketTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public final class WebsocketTest {
	static Logger LOG;

	static String PROPERTIES_FILE_NAME = "application.properties";

	static {
		LOG = LoggerFactory.getLogger(WebsocketTest.class);
	}

	@Test
	public void test() {

		KeyPair keyPair;

		try {
			Security.addProvider(new BouncyCastleProvider());
			
			InputStream propInputStream = WebsocketTest.class.getClassLoader()
					.getResourceAsStream(PROPERTIES_FILE_NAME);

			Properties env = new Properties();
			env.load(propInputStream);

			keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();

			byte[] publicKeyBytes = Utils.getPublicKeyBytes(keyPair);

			BasicIdentity identity = BasicIdentity.fromKeyPair(keyPair);

			ReplicaTransport transport = ReplicaOkHttpTransport.create(env.getProperty("ic.location"));

			Agent agent = new AgentBuilder().transport(transport).identity(identity).build();

			// open websocket
			final WebsocketTransport wsTransport = new JavaxWebsocketTransport(new URI(env.getProperty("ws.location")));

			WebsocketClient wsClient = new WebsocketClient(agent, Principal.fromString(env.getProperty("ic.canister")),
					wsTransport, identity, publicKeyBytes).setLocal(Boolean.parseBoolean(env.getProperty("local","false")));
			
			wsClient
			.addMessageHandler((output) -> {
						try {
							System.out.println(Agent.cborToJson(output));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					})
			.addMessageHandler((output) -> {
						try {
							Thread.sleep(1000);
							ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());
							ObjectNode jsonNode = objectMapper.createObjectNode();
							jsonNode.put("text", "pong").put("timestamp", System.currentTimeMillis());
							ObjectWriter objectWriter = objectMapper.writerFor(ObjectNode.class);
							byte[] buf = null;
							try {
								buf = objectWriter.writeValueAsBytes(jsonNode);
								wsClient.sendApplicationMessage(buf);

							} catch (JsonProcessingException e) {
									throw new WebsocketError(e);
							}
							
							System.out.println(Agent.cborToJson(output));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					

			wsClient.start();

			// wait 5 seconds for messages from websocket
			Thread.sleep(50000);

		} catch (InterruptedException e) {
			LOG.debug(e.getLocalizedMessage(), e);
			Assertions.fail(e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			LOG.debug(e.getLocalizedMessage(), e);
			Assertions.fail(e.getLocalizedMessage());
		} catch (NoSuchAlgorithmException e) {
			LOG.debug(e.getLocalizedMessage(), e);
			Assertions.fail(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.debug(e.getLocalizedMessage(), e);
			Assertions.fail(e.getLocalizedMessage());
		}
	}

}
