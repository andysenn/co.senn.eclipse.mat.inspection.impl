/*
 * Copyright 2018 Andy Senn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.senn.eclipse.mat.inspection.hikari;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

import co.senn.eclipse.mat.inspection.api.ITechnology;

public class HikariCP implements ITechnology {

	@Override
	public boolean isPresent(ISnapshot snapshot) throws SnapshotException {
		return ITechnology.isPackagePresent(snapshot, "com.zaxxer.hikari");
	}

}
