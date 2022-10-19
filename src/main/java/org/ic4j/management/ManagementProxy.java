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

import java.util.concurrent.CompletableFuture;

import org.ic4j.agent.annotations.UPDATE;
import org.ic4j.agent.annotations.Waiter;
import org.ic4j.candid.annotations.Name;


public interface ManagementProxy {
	@UPDATE
	@Name("create_canister")
	@Waiter(timeout = 300)
	public CompletableFuture<CreateCanisterResponse> createCanister(CreateCanisterRequest createCanisterRequest);
	
	@UPDATE
	@Name("update_settings")
	@Waiter(timeout = 300)
	public void updateSettings(UpdateSettingsRequest updateSettingsRequest);	
	
	@UPDATE
	@Name("install_code")
	@Waiter(timeout = 300)
	public void installCode(InstallCodeRequest installCodeRequest);	
	
	@UPDATE
	@Name("uninstall_code")
	@Waiter(timeout = 300)
	public void uninstallCode(UninstallCodeRequest uninstallCodeRequest);	
	
	@UPDATE
	@Name("start_canister")
	@Waiter(timeout = 30)
	public void  startCanister(StartCanisterRequest startCanisterRequest);	
	
	@UPDATE
	@Name("stop_canister")
	@Waiter(timeout = 30)
	public void stopCanister(StopCanisterRequest stopCanisterRequest);		
	
	@UPDATE
	@Name("canister_status")
	@Waiter(timeout = 30)
	public CompletableFuture<CanisterStatusResponse> canisterStatus(CanisterStatusRequest canisterStatusRequest);	
	
	@UPDATE
	@Name("delete_canister")
	@Waiter(timeout = 30)
	public void deleteCanister(DeleteCanisterRequest deleteCanisterRequest);		

	@UPDATE
	@Name("deposit_cycles")
	@Waiter(timeout = 30)
	public void depositCycles(DepositCyclesRequest depositCyclesRequest);
	
	@UPDATE
	@Name("raw_rand")
	@Waiter(timeout = 30)
	public CompletableFuture<byte[]> rawRand();		
	
	// provisional interfaces for the pre-ledger world
	
	@UPDATE
	@Name("provisional_create_canister_with_cycles")
	@Waiter(timeout = 300)
	public CompletableFuture<CreateCanisterResponse> provisionalCreateCanisterWithCycles(ProvisionalCreateCanisterWithCyclesRequest provisionalCreateCanisterWithCyclesRequest);

	@UPDATE
	@Name("provisional_top_up_canister")
	@Waiter(timeout = 300)
	public void provisionalTopUpCanister(ProvisionalTopUpCanisterRequest provisionalTopUpCanisterRequest);
	
}
