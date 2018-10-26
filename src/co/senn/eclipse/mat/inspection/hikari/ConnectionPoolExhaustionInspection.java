package co.senn.eclipse.mat.inspection.hikari;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.query.results.TextResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.AbstractInspection;
import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;

public class ConnectionPoolExhaustionInspection extends AbstractInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Map<IObject, Integer> waitersByPool = new HashMap<>();
		boolean found = forEachObjectOfType("com.zaxxer.hikari.pool.HikariPool", true, snapshot, object -> {
			Object waiters = object.resolveValue("connectionBag.waiters.value");
			tryParseInt(waiters, i -> {
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

		return new InspectionResult(new TextResult(sb.toString()), InspectionResultSeverity.WARN);
	}

}
