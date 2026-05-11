package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // Line 1: e.g. "GET /api/resource?id=123 HTTP/1.1"
        String firstLine = reader.readLine();
        String[] parts = firstLine.split(" ");
        String httpCommand = parts[0];
        String uri = parts[1];

        // URI segments: path part (before ?) split by /, empty strings filtered out
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
        List<String> segList = new ArrayList<>();
        for (String seg : path.split("/")) {
            if (!seg.isEmpty()) segList.add(seg);
        }
        String[] uriSegments = segList.toArray(new String[0]);

        // Query string parameters
        Map<String, String> parameters = new HashMap<>();
        if (uri.contains("?")) {
            String query = uri.substring(uri.indexOf("?") + 1);
            for (String param : query.split("&")) {
                int eq = param.indexOf("=");
                if (eq >= 0) parameters.put(param.substring(0, eq), param.substring(eq + 1));
            }
        }

        // Read headers until first empty line
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            // consume headers; Content-Length not used for blocking reads
        }

        // Body parameters (key=value or key="value") until second empty line
        while (reader.ready() && (line = reader.readLine()) != null && !line.isEmpty()) {
            int eq = line.indexOf("=");
            if (eq >= 0) parameters.put(line.substring(0, eq), line.substring(eq + 1));
        }

        // Body content until empty line or reader not ready
        StringBuilder sb = new StringBuilder();
        while (reader.ready() && (line = reader.readLine()) != null && !line.isEmpty()) {
            sb.append(line).append("\n");
        }
        byte[] content = sb.toString().getBytes();

        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
    }

    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments,
                           Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() { return httpCommand; }
        public String getUri() { return uri; }
        public String[] getUriSegments() { return uriSegments; }
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent() { return content; }
    }
}
