package co.senn.eclipse.mat.inspection.tomcat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.query.results.ListResult;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import co.senn.eclipse.mat.inspection.AbstractInspection;
import co.senn.eclipse.mat.inspection.InspectionResult;
import co.senn.eclipse.mat.inspection.api.IInspectionResult;
import co.senn.eclipse.mat.inspection.api.InspectionResultSeverity;
import co.senn.eclipse.mat.util.PrimitiveValueUtil;

@CommandName("http:requests-tomcat")
public class TomcatHTTPRequestQuery extends AbstractInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Collection<IObject> requests = getObjects("org.apache.coyote.Request", true, snapshot);

		List<TomcatHTTPRequestQueryResult> results = new ArrayList<>();
		for (IObject request : requests) {
			// @formatter:off
			results.add(new TomcatHTTPRequestQueryResult(
					parseMessageBytes(request, "serverNameMB"),
					parseMessageBytes(request, "methodMB"),
					parseMessageBytes(request, "uriMB"),
					request.getUsedHeapSize(),
					request.getRetainedHeapSize()
			));
			// @formatter:on
		}

		if (requests.size() == 0) {
			return null;
		}

		return new InspectionResult(new ListResult(TomcatHTTPRequestQueryResult.class, results, "host", "method", "uri",
				"shallowHeap", "retainedHeap"), InspectionResultSeverity.INFO);
	}

	private String parseMessageBytes(IObject request, String field) throws SnapshotException {
		IObject messageBytes = (IObject) request.resolveValue(field);
		if (messageBytes == null) {
			return "null";
		}

		// Try "byteChunk"
		IObject byteChunk = (IObject) messageBytes.resolveValue("byteC");
		if (byteChunk == null) {
			return "null";
		}

		boolean byteChunkSet = (boolean) byteChunk.resolveValue("isSet");
		if (byteChunkSet) {
			byte[] bytes = PrimitiveValueUtil.getByteArray(byteChunk, "buff");
			int start = (int) byteChunk.resolveValue("start");
			int end = (int) byteChunk.resolveValue("end");

			return new String(bytes, start, end - start);
		}

		// Try "charChunk"
		IObject charChunk = (IObject) messageBytes.resolveValue("charC");
		if (charChunk == null) {
			return "null";
		}

		boolean charChunkSet = (boolean) charChunk.resolveValue("isSet");
		if (charChunkSet) {
			char[] chars = PrimitiveValueUtil.getCharArray(charChunk, "buff");
			int start = (int) charChunk.resolveValue("start");
			int end = (int) charChunk.resolveValue("end");

			return new String(chars, start, end - start);
		}

		return "null";
	}

	public static class TomcatHTTPRequestQueryResult {

		private final String host;
		private final String method;
		private final String uri;
		private final long shallowHeap;
		private final long retainedHeap;

		public TomcatHTTPRequestQueryResult(String host, String method, String uri, long shallowHeap,
				long retainedHeap) {
			this.host = host;
			this.method = method;
			this.uri = uri;
			this.shallowHeap = shallowHeap;
			this.retainedHeap = retainedHeap;
		}

		public String getHost() {
			return host;
		}

		public String getMethod() {
			return method;
		}

		public String getUri() {
			return uri;
		}

		public long getShallowHeap() {
			return shallowHeap;
		}

		public long getRetainedHeap() {
			return retainedHeap;
		}

	}

}
