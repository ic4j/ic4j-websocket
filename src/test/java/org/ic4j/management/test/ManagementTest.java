package org.ic4j.management.test;

import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.AgentError;
import org.ic4j.agent.ProxyBuilder;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaApacheHttpTransport;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.agent.identity.Identity;
import org.ic4j.management.CanisterStatusResponse;
import org.ic4j.management.ManagementService;
import org.ic4j.management.Mode;
import org.ic4j.types.Principal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManagementTest {
	static Logger LOG;

	static String PROPERTIES_FILE_NAME = "application.properties";
	protected static String WASM_FILE = "hello.wasm";	

	static {
		LOG = LoggerFactory.getLogger(ManagementTest.class);
	}

	@Test
	public void test() {

		try {
			Security.addProvider(new BouncyCastleProvider());

			InputStream propInputStream = ManagementTest.class.getClassLoader()
					.getResourceAsStream(PROPERTIES_FILE_NAME);

			Properties env = new Properties();
			env.load(propInputStream);

			String managementLocation = env.getProperty("management.location");
			String managementIdentityFile = env.getProperty("management.identity");
			
			LOG.info(managementLocation);

			ReplicaTransport transport = ReplicaApacheHttpTransport.create(managementLocation);

			// Use Basic Identity 	
			
			Reader sourceReader = new FileReader(managementIdentityFile);
			
			Identity identity = BasicIdentity.fromPEMFile(sourceReader);

			Agent agent = new AgentBuilder().transport(transport)
					.identity(identity)
					.build();
			
			agent.fetchRootKey();

			ManagementService managementService = ManagementService.create(agent, Principal.managementCanister(),Principal.fromString("x5pps-pqaaa-aaaab-qadbq-cai"));				
			
			Principal canisterId = managementService.provisionalCreateCanisterWithCycles(Optional.empty(), Optional.empty()).get();

			LOG.info(canisterId.toString());
			
			Path path = Paths.get(getClass().getClassLoader().getResource(WASM_FILE).getPath());
			
			byte [] wasmModule = Files.readAllBytes(path);	
			
			managementService.installCode(canisterId, Mode.install, wasmModule, ArrayUtils.EMPTY_BYTE_ARRAY);
			
			CanisterStatusResponse canisterStatusResponse = managementService.canisterStatus(canisterId).get();
			
			LOG.info(canisterStatusResponse.status.name());			
			
			HelloWorldProxy helloWorldProxy = ProxyBuilder.create(agent, canisterId)
					.getProxy(HelloWorldProxy.class);

			String value = "world";
			
			CompletableFuture<String> proxyResponse = helloWorldProxy.greet(value);
			
			String output = proxyResponse.get();
			LOG.info(output);
			
			managementService.stopCanister(canisterId);	
			
			canisterStatusResponse =  managementService.canisterStatus(canisterId).get();
			
			LOG.info(canisterStatusResponse.status.name());		
			
			managementService.uninstallCode(canisterId);			

			managementService.deleteCanister(canisterId);			
			
		}
		catch (AgentError e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}		
		catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}
	}

}
