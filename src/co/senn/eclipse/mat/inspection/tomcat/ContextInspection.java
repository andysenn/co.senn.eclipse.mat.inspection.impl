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
package co.senn.eclipse.mat.inspection.tomcat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.results.ListResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspection;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;
import co.senn.eclipse.mat.inspection.util.InspectionUtil;

@CommandName("suspect:servlet-container-contexts")
public class ContextInspection implements IInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Collection<IObject> contexts = InspectionUtil.getObjects("org.apache.catalina.loader.WebappClassLoader", true,
				snapshot);

		List<ContextResult> results = new ArrayList<>();
		for (IObject context : contexts) {
			String state = "Unknown";
			getState: {
				// Older Tomcat Versions
				Object started = context.resolveValue("started");
				if (started != null) {
					state = Objects.equals(started, true) ? "Started" : "Stopped";
					break getState;
				}

				// Newer Tomcat Versions
				Object stateName = context.resolveValue("state.name");
				if (stateName != null) {
					state = String.valueOf(stateName);
					break getState;
				}
			}

			results.add(new ContextResult("<no name>", state));
		}

		if (results.size() == 0) {
			return null;
		}

		return new InspectionResult(new ListResult(ContextResult.class, results, "name", "state"),
				"Found " + results.size() + " contexts", InspectionResultSeverity.INFO);
	}

	private static class ContextResult {

		private final String name;
		private final String state;

		public ContextResult(String name, String state) {
			this.name = name;
			this.state = state;
		}

		public String getName() {
			return name;
		}

		public String getState() {
			return state;
		}

	}

}
