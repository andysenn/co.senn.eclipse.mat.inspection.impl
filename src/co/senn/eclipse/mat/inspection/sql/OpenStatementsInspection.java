package co.senn.eclipse.mat.inspection.sql;

import org.eclipse.mat.query.results.TextResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.AbstractInspection;
import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;

public class OpenStatementsInspection extends AbstractInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		StringBuilder sb = new StringBuilder();

		boolean found = forEachObjectOfType("java.sql.Statement", true, snapshot, s -> {
			sb.append("\n").append(s);
		});

		if (!found) {
			return new InspectionResult(new TextResult("No results"), InspectionResultSeverity.INFO);
		}

		return new InspectionResult(new TextResult(sb.toString()), InspectionResultSeverity.INFO);
	}

}
