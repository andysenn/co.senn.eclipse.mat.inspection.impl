package co.senn.eclipse.mat.inspection;

import org.eclipse.mat.query.IResult;

import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;

public class InspectionResult implements IInspectionResult {

	private final IResult result;
	private final InspectionResultSeverity severity;

	public InspectionResult(IResult result, InspectionResultSeverity severity) {
		this.result = result;
		this.severity = severity;
	}

	@Override
	public IResult getResult() {
		return result;
	}

	@Override
	public InspectionResultSeverity getSeverity() {
		return severity;
	}

}
