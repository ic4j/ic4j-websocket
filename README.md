## IC4J Websocket Client

This Java library implements the ICP Websocket Gateway protocol, allowing any Java application to receive Websocket messages from ICP smart contracts.
 
 <a href="https://github.com/omnia-network/ic-websocket-gateway">
https://github.com/omnia-network/ic-websocket-gateway
</a>
 

# Build

You need JDK 8+ to build IC4J Websocket Agent.

# License

IC4J Websocket is available under Apache License 2.0.


# Documentation

```
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
```

# Downloads / Accessing Binaries

To add IC4J Websocket library to your Java project use Maven or Gradle import from Maven Central.

<a href="https://search.maven.org/artifact/org.ic4j/ic4j-websocket/0.7.0/jar">
https://search.maven.org/artifact/org.ic4j/ic4j-websocket/0.7.0/jar
</a>

```
<dependency>
  <groupId>org.ic4j</groupId>
  <artifactId>ic4j-websocket</artifactId>
  <version>0.7.0</version>
</dependency>
```

```
implementation 'org.ic4j:ic4j-websocket:0.7.0'
```
