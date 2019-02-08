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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.query.results.TextResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspection;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;
import co.senn.eclipse.mat.inspection.util.InspectionUtil;
import co.senn.eclipse.mat.inspection.util.PrimitiveValueUtil;

public class ConnectionPoolExhaustionInspection implements IInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Map<IObject, Integer> waitersByPool = new HashMap<>();
		boolean found = InspectionUtil.forEachObjectOfType("com.zaxxer.hikari.pool.HikariPool", true, snapshot, object -> {
			Object waiters = object.resolveValue("connectionBag.waiters.value");
			PrimitiveValueUtil.tryParseInt(waiters, i -> {
				if (i > 0) {
					waitersByPool.put(object, i);
				}
			});
		});

		if (!found || waitersByPool.size() == 0) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Found ").append(waitersByPool.size()).append(" pool(s) with threads awaiting connections:\n");
		waitersByPool.forEach((object, waiters) -> {
			sb.append("\n").append(object).append(": ").append(waiters).append(" threads awaiting a connection");
		});
		
		return new InspectionResult(new TextResult(sb.toString()), "", InspectionResultSeverity.WARN);
	}

}
