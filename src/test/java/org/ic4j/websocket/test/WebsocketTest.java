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
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaOkHttpTransport;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.pojo.PojoDeserializer;
import org.ic4j.types.Principal;

import org.ic4j.websocket.JavaxWebsocketTransport;
import org.ic4j.websocket.WsAgent;
import org.ic4j.websocket.WebsocketTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebsocketTest {
	static Logger LOG;

	static String PROPERTIES_FILE_NAME = "application.properties";

	static {
		LOG = LoggerFactory.getLogger(WebsocketTest.class);
	}

	@Test
	public void test() {

		;

		try {
			Security.addProvider(new BouncyCastleProvider());
			
			InputStream propInputStream = WebsocketTest.class.getClassLoader()
					.getResourceAsStream(PROPERTIES_FILE_NAME);

			Properties env = new Properties();
			env.load(propInputStream);

			KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();

			BasicIdentity identity = BasicIdentity.fromKeyPair(keyPair);

			ReplicaTransport httpTransport = ReplicaOkHttpTransport.create(env.getProperty("ic.location"));
			
			final WebsocketTransport wsTransport = new JavaxWebsocketTransport(new URI(env.getProperty("ws.location")), httpTransport);
			

			WsAgent wsAgent = new WsAgent( Principal.fromString(env.getProperty("ic.canister")),
					wsTransport, identity).setLocal(Boolean.parseBoolean(env.getProperty("local","false")));
			
			wsAgent
			.addMessageHandler((output) -> {
						try {
							AppMessage result = IDLArgs.fromBytes(output).getArgs().get(0).getValue(new PojoDeserializer(), AppMessage.class);
							LOG.info(result.text);
							
							AppMessage response = new AppMessage();
							
							response.text = "Pong";
							
							response.timestamp = System.currentTimeMillis();
							
							byte[] payload = WsAgent.encodeValue(response);
							
							wsAgent.sendApplicationMessage(payload);
						} catch (Exception e) {
							LOG.error(e.getLocalizedMessage(),e);
						}
					});	

			wsAgent.start();

			// wait 100 seconds for messages from websocket
			Thread.sleep(100000);
			
			wsAgent.close();
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
