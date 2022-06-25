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

public class ManagementError extends Error {
	private static final long serialVersionUID = -5686403499138076209L;
	ManagementErrorCode code;

	public ManagementError() {

	}

	public ManagementError(String message) {
		super(message);
	}

	public ManagementError(Throwable cause) {
		super(cause);
	}

	public ManagementError(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ManagementError(ManagementErrorCode code) {
		this.code = code;
	}

	public ManagementError(String message, ManagementErrorCode code) {	
		super(message);
		this.code = code;
	}	
	
	public ManagementErrorCode getCode()
	{
		return this.code;
	}
	
	public enum ManagementErrorCode {
	}

}
