package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import server.MyHTTPServer;
import server.RequestParser;
import server.RequestParser.RequestInfo;
import server.Servlet;

public class MainTrain {

    private static void testParseRequest() {
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 5\n" +
                "\n" +
                "filename=\"hello_world.txt\"\n" +
                "\n" +
                "hello world!\n" +
                "\n";

        BufferedReader input = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestInfo requestInfo = RequestParser.parseRequest(input);

            if (!requestInfo.getHttpCommand().equals("GET"))
                System.out.println("HTTP command test failed (-5)");

            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test"))
                System.out.println("URI test failed (-5)");

            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments))
                System.out.println("URI segments test failed (-5)");

            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename", "\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams))
                System.out.println("Parameters test failed (-5)");

            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent))
                System.out.println("Content test failed (-5)");

            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }
    }

    public static void testServer() throws Exception {
        MyHTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/api/", new Servlet() {
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                String id = ri.getParameters().get("id");
                PrintWriter pw = new PrintWriter(toClient);
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type: text/plain");
                pw.println();
                pw.println("id=" + id);
                pw.flush();
            }
            public void close() throws IOException {}
        });

        int before = Thread.activeCount();
        server.start();
        if (Thread.activeCount() != before + 1)
            System.out.println("server did not open exactly one thread (-10)");

        Thread.sleep(200);
        Socket client = new Socket("localhost", 8080);
        PrintWriter out = new PrintWriter(client.getOutputStream());
        out.print("GET /api/resource?id=42 HTTP/1.1\r\n");
        out.print("Host: localhost\r\n");
        out.print("\r\n");
        out.flush();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
        }
        client.close();

        if (!response.toString().contains("id=42"))
            System.out.println("server did not return expected result (-10)");

        server.close();
        Thread.sleep(2000);

        if (Thread.activeCount() != before)
            System.out.println("server did not close all threads (-10)");
    }

    public static void main(String[] args) {
        testParseRequest();
        try {
            testServer();
        } catch (Exception e) {
            System.out.println("your server threw an exception (-60)");
        }
        System.out.println("done");
    }
}
