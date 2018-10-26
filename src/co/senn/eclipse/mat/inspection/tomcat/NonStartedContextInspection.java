package co.senn.eclipse.mat.inspection.tomcat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.results.TextResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.AbstractInspection;
import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;

/**
 * Checks for non-started Tomcat web application contexts.
 * <p>
 * The presence of these can be indicative of a memory leak (eg: uninterruptible
 * threads).
 * 
 * @author Andy Senn
 */
@CommandName("suspect:servlet-container-non-started-contexts")
public class NonStartedContextInspection extends AbstractInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Collection<IObject> nonStartedClassLoaders = new ArrayList<>();
		boolean found = forEachObjectOfType("org.apache.catalina.loader.WebappClassLoader", true, snapshot, object -> {

			// Older Tomcat Versions
			Object started = object.resolveValue("started");
			if (started != null && Objects.equals(started, false)) {
				nonStartedClassLoaders.add(object);
				return; // Instead of nesting if/else blocks
			}

			// Newer Tomcat Versions
			Object state = object.resolveValue("state.name");
			if (started != null && Objects.equals(state, "DESTROYED")) {
				nonStartedClassLoaders.add(object);
				return; // Instead of nesting if/else blocks
			}

			// Any other things to check here?
		});

		if (!found || nonStartedClassLoaders.size() == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Found ").append(nonStartedClassLoaders.size())
				.append(" non-started context(s), which may be indicitive of a memory leak:\n");
		nonStartedClassLoaders.forEach(object -> sb.append("\n").append(object));

		return new InspectionResult(new TextResult(sb.toString()), InspectionResultSeverity.WARN);
	}

}
