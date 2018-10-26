package co.senn.eclipse.mat.inspection.sql;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

import co.senn.eclipse.mat.inspection.api.ITechnology;
import co.senn.eclipse.mat.inspection.api.Ignore;

@Ignore
public class SQL implements ITechnology {

	@Override
	public boolean isPresent(ISnapshot snapshot) throws SnapshotException {
		return isPackagePresent(snapshot, "java.sql");
	}

}
