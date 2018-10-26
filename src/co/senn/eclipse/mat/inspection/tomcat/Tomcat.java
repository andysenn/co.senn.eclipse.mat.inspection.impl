package co.senn.eclipse.mat.inspection.tomcat;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

import co.senn.eclipse.mat.inspection.api.ITechnology;

public class Tomcat implements ITechnology {

	@Override
	public boolean isPresent(ISnapshot snapshot) throws SnapshotException {
		return isPackagePresent(snapshot, "org.apache.tomcat");
	}

}
