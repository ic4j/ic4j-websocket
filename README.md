## IC4J Management Java Service 

This library is a Java implementation of Dfinity Virtual Canister Management API. 
 The library communicates directly with Management Virtual canister.
 
 <a href="https://internetcomputer.org/docs/current/references/ic-interface-spec#ic-management-canister">
https://internetcomputer.org/docs/current/references/ic-interface-spec#ic-management-canister
</a>
 
 Developers can use this library in any Java application to create a new canister, delete canister, install, uninstall and reinstall WASM canister code, start and stop canister, update canister settings and add additional funds to canister.

To test Management Service locally, install local Internet Computer instance. Then just configure custom Java properties with management.location value pointing to the local instance and management.identity pointing to identity PEM file.

To use Management Service create ManagementService object. Define your own properties if you want to use local instance of the service. 

```
Identity identity = BasicIdentity.fromPEMFile(sourceReader);

Agent agent = new AgentBuilder().transport(transport)
			.identity(identity)
			.build();

ManagementService managementService = ManagementService.create(agent);
```
Then call Management Service methods.  



```
Principal canisterId = managementService.provisionalCreateCanisterWithCycles(Optional.empty(), Optional.empty()).get();

LOG.info(canisterId.toString());
			
Path path = Paths.get(getClass().getClassLoader().getResource(WASM_FILE).getPath());
			
byte [] wasmModule = Files.readAllBytes(path);	
			
managementService.installCode(canisterId, Mode.install, wasmModule, ArrayUtils.EMPTY_BYTE_ARRAY);
			
CanisterStatusResponse canisterStatusResponse =  managementService.canisterStatus(canisterId).get();
			
LOG.info(canisterStatusResponse.status.name());						
			
managementService.stopCanister(canisterId);	
			
canisterStatusResponse =  managementService.canisterStatus(canisterId).get();
			
LOG.info(canisterStatusResponse.status.name());		
			
managementService.uninstallCode(canisterId);			

managementService.deleteCanister(canisterId);
```


# Downloads / Accessing Binaries

To add Java IC4J Management Service library to your Java project use Maven or Gradle import from Maven Central.

<a href="https://search.maven.org/artifact/ic4j/ic4j-management/0.6.18/jar">
https://search.maven.org/artifact/ic4j/ic4j-management/0.6.18/jar
</a>

```
<dependency>
  <groupId>org.ic4j</groupId>
  <artifactId>ic4j-management</artifactId>
  <version>0.6.18</version>
</dependency>
```

```
implementation 'org.ic4j:ic4j-management:0.6.18'
```


# Build

You need JDK 8+ to build IC4J Management Service.
