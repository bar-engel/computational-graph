package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.RequestParser.RequestInfo;

public class MyHTTPServer extends Thread implements HTTPServer {

    private final int port;
    private final int nThreads;
    private volatile boolean running = false;
    private ExecutorService threadPool;
    private final Map<String, Servlet> getServlets    = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets   = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
        this.nThreads = nThreads;
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        mapFor(httpCommand).put(uri, s);
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        mapFor(httpCommand).remove(uri);
    }

    private Map<String, Servlet> mapFor(String httpCommand) {
        switch (httpCommand) {
            case "POST":   return postServlets;
            case "DELETE": return deleteServlets;
            default:       return getServlets;
        }
    }

    @Override
    public void run() {
        threadPool = Executors.newFixedThreadPool(nThreads);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    threadPool.submit(() -> handleClient(client));
                } catch (SocketTimeoutException e) {
                    // expected: re-check running
                } catch (IOException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            // server socket could not be opened
        } finally {
            if (threadPool != null) threadPool.shutdown();
        }
    }

    private void handleClient(Socket client) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            RequestInfo ri = RequestParser.parseRequest(reader);
            Servlet servlet = findServlet(mapFor(ri.getHttpCommand()), ri.getUri());
            if (servlet != null) servlet.handle(ri, client.getOutputStream());
        } catch (Exception e) {
            // ignore client errors
        } finally {
            try { client.close(); } catch (IOException e) {}
        }
    }

    private Servlet findServlet(Map<String, Servlet> map, String uri) {
        String bestKey = null;
        for (String key : map.keySet()) {
            if (uri.startsWith(key)) {
                if (bestKey == null || key.length() > bestKey.length()) bestKey = key;
            }
        }
        return bestKey != null ? map.get(bestKey) : null;
    }

    @Override
    public void start() {
        running = true;
        super.start();
    }

    @Override
    public void close() {
        running = false;
        this.interrupt();
    }
}
