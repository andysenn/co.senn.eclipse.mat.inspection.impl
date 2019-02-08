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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.mat.SnapshotException;
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
import co.senn.eclipse.mat.inspection.util.PrimitiveValueUtil;

@CommandName("http:requests-tomcat")
public final class HTTPRequestListInspection implements IInspection {

	@Override
	public IInspectionResult execute(ISnapshot snapshot, IProgressListener listener) throws Exception {
		Collection<IObject> requests = InspectionUtil.getObjects("org.apache.coyote.Request", true, snapshot);

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

		List<TomcatMergedHTTPRequestQueryResult> mergedResults = merge(results);
		Collections.sort(mergedResults, (r1, r2) -> r2.getCount() - r1.getCount());

		return new InspectionResult(
				new ListResult(TomcatMergedHTTPRequestQueryResult.class, mergedResults, "host", "method", "uri",
						"shallowHeap", "retainedHeap", "count"),
				"Found " + results.size() + " HTTP requests", InspectionResultSeverity.INFO);
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

	private List<TomcatMergedHTTPRequestQueryResult> merge(Collection<TomcatHTTPRequestQueryResult> results) {
		Map<String, List<TomcatHTTPRequestQueryResult>> result = results.stream()
				.collect(Collectors.groupingBy(r -> r.getHost() + r.getMethod() + r.getUri()));

		// @formatter:off
		List<TomcatMergedHTTPRequestQueryResult> mergedResults = new ArrayList<>();
		result.forEach((id, res) -> {
			long shallowHeapTotal = 0;
			long retainedHeapTotal = 0;
			for (TomcatHTTPRequestQueryResult request : res) {
				shallowHeapTotal += request.getShallowHeap();
				retainedHeapTotal += request.getRetainedHeap();
			}

			TomcatHTTPRequestQueryResult first = res.get(0);
			mergedResults.add(new TomcatMergedHTTPRequestQueryResult(
					first.getHost(),
					first.getMethod(),
					first.getUri(),
					shallowHeapTotal,
					retainedHeapTotal,
					res.size()
			));
		});
		// @formatter:on

		return mergedResults;
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

	public static class TomcatMergedHTTPRequestQueryResult extends TomcatHTTPRequestQueryResult {

		public final int count;

		public TomcatMergedHTTPRequestQueryResult(String host, String method, String uri, long shallowHeap,
				long retainedHeap, int count) {
			super(host, method, uri, shallowHeap, retainedHeap);
			this.count = count;
		}

		public int getCount() {
			return count;
		}

	}

}
