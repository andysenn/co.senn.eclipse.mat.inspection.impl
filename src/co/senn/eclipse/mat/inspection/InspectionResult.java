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
package co.senn.eclipse.mat.inspection;

import org.eclipse.mat.query.IResult;

import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;

public final class InspectionResult implements IInspectionResult {

	private final IResult result;
	private final String resultSummary;
	private final InspectionResultSeverity severity;

	public InspectionResult(IResult result, String resultSummary, InspectionResultSeverity severity) {
		this.result = result;
		this.resultSummary = resultSummary;
		this.severity = severity;
	}

	@Override
	public IResult getResult() {
		return result;
	}

	@Override
	public String getResultSummary() {
		return resultSummary;
	}

	@Override
	public InspectionResultSeverity getSeverity() {
		return severity;
	}

}
