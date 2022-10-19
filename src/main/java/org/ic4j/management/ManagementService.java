/*
 * Copyright 2021 Exilor Inc.
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

package org.ic4j.management;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.ArrayUtils;
import org.ic4j.agent.Agent;
import org.ic4j.agent.ProxyBuilder;
import org.ic4j.types.Principal;

public final class ManagementService {
	Principal managementCanister;

	Agent agent;

	ManagementProxy managementProxy;


	public static ManagementService create(Agent agent) throws ManagementError {
		return create(agent, Principal.managementCanister(),Principal.managementCanister());
	}

	public static ManagementService create(Agent agent, Principal managementCanister, Principal effectiveCanister) throws ManagementError {
		
		ManagementService managementService = new ManagementService();

		managementService.managementCanister = managementCanister;

		managementService.agent = agent;

		managementService.managementProxy = ProxyBuilder
				.create(agent, managementService.managementCanister)
				.effectiveCanisterId(effectiveCanister)
				.getProxy(ManagementProxy.class);

		return managementService;
	}
	 
	public CompletableFuture<Principal> createCanister(Optional<CanisterSettings> settings) {
		CompletableFuture<Principal> response = new CompletableFuture<Principal>(); 
		
		CreateCanisterRequest createCanisterRequest = new CreateCanisterRequest();
		createCanisterRequest.settings = settings;
		managementProxy.createCanister(createCanisterRequest).whenComplete((createCanisterResponse, ex) -> {
			if (ex == null) 
				if (createCanisterResponse != null) 
						response.complete(createCanisterResponse.canisterId);
				else
					response.completeExceptionally(new ManagementError("Empty Response"));
			else
				response.completeExceptionally(new ManagementError(ex));

		});
		
		return response;
	}

	public void updateSettings(Principal canisterId, CanisterSettings settings) {
		UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest();
		updateSettingsRequest.canisterId = canisterId;
		updateSettingsRequest.settings = settings;
		
		managementProxy.updateSettings(updateSettingsRequest);
	}
	
	public void installCode(Principal canisterId, Mode mode, byte[] wasmModule, byte[] arg) {
		InstallCodeRequest installCodeRequest = new InstallCodeRequest();
		installCodeRequest.canisterId = canisterId;
		installCodeRequest.mode = mode;
		installCodeRequest.wasmModule = wasmModule;
		installCodeRequest.arg = arg;
		if(installCodeRequest.arg == null)
			installCodeRequest.arg = ArrayUtils.EMPTY_BYTE_ARRAY;
		managementProxy.installCode(installCodeRequest);
	}
	
	public void uninstallCode(Principal canisterId) {
		UninstallCodeRequest uninstallCodeRequest = new UninstallCodeRequest();
		uninstallCodeRequest.canisterId = canisterId;
		managementProxy.uninstallCode(uninstallCodeRequest);
	}	

	public void deleteCanister(Principal canisterId) {
		DeleteCanisterRequest deleteCanisterRequest = new DeleteCanisterRequest();
		deleteCanisterRequest.canisterId = canisterId;
		managementProxy.deleteCanister(deleteCanisterRequest);
	}
	
	public void startCanister(Principal canisterId) {
		StartCanisterRequest startCanisterRequest = new StartCanisterRequest();
		startCanisterRequest.canisterId = canisterId;
		managementProxy.startCanister(startCanisterRequest);
	}
	
	public void stopCanister(Principal canisterId) {
		StopCanisterRequest stopCanisterRequest = new StopCanisterRequest();
		stopCanisterRequest.canisterId = canisterId;
		managementProxy.stopCanister(stopCanisterRequest);
	}
	
	public void depositCycles(Principal canisterId) {
		DepositCyclesRequest depositCyclesRequest = new DepositCyclesRequest();
		depositCyclesRequest.canisterId = canisterId;
		managementProxy.depositCycles(depositCyclesRequest);
	}	
	
	public CompletableFuture<CanisterStatusResponse> canisterStatus(Principal canisterId) {
		CanisterStatusRequest canisterStatusRequest = new CanisterStatusRequest();
		canisterStatusRequest.canisterId = canisterId;
		return managementProxy.canisterStatus(canisterStatusRequest);
	}	
	
	public CompletableFuture<byte[]> rawRand() {
		return managementProxy.rawRand();
	}	
	
	public CompletableFuture<Principal> provisionalCreateCanisterWithCycles(Optional<CanisterSettings> settings, Optional<BigInteger> amount) {
		CompletableFuture<Principal> response = new CompletableFuture<Principal>(); 
		
		ProvisionalCreateCanisterWithCyclesRequest provisionalCreateCanisterWithCyclesRequest = new ProvisionalCreateCanisterWithCyclesRequest();
		provisionalCreateCanisterWithCyclesRequest.settings = settings;
		provisionalCreateCanisterWithCyclesRequest.amount = amount;
		managementProxy.provisionalCreateCanisterWithCycles(provisionalCreateCanisterWithCyclesRequest).whenComplete((createCanisterResponse, ex) -> {
			if (ex == null) 
				if (createCanisterResponse != null) 
						response.complete(createCanisterResponse.canisterId);
				else
					response.completeExceptionally(new ManagementError("Empty Response"));
			else
				response.completeExceptionally(new ManagementError(ex));

		});
		
		return response;
		
	}
	
	public void provisionalTopUpCanister(Principal canisterId, BigInteger amount) {
		ProvisionalTopUpCanisterRequest provisionalTopUpCanisterRequest = new ProvisionalTopUpCanisterRequest();
		provisionalTopUpCanisterRequest.canisterId = canisterId;
		provisionalTopUpCanisterRequest.amount = amount;
		managementProxy.provisionalTopUpCanister(provisionalTopUpCanisterRequest);
	}	
}
