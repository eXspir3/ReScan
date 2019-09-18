import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReScanHandler {
    private Map<String, String> requestMap;
    private File requests;
    private Table<String, String, String> loggedErrors;
    private TcpRawHttpClient client;
    private RawHttp http;
    private Integer noLogs = 0;

    ReScanHandler(File requests) throws FileNotFoundException {
        this.requests = requests;
        this.client = new TcpRawHttpClient();
        this.http = new RawHttp();
        this.requestMap = new HashMap<>();
        this.loggedErrors = HashBasedTable.create();
        this.importRequests();
    }

    void replayWithOptions() throws IOException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            checkResponseOptions(entry.getValue(), entry.getKey(), response);
        }
        saveResults();
    }

    void replayNoOptions() throws IOException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            noLogs++;
            loggedErrors.put("\n\n" + noLogs.toString(), "\n\n" + noLogs.toString() + ". Request: \n\n" + request.toString(),
                    noLogs.toString() + ". Response: \n\n" + response.toString());
        }
        saveResults();
    }

    private void importRequests() throws FileNotFoundException {
        String request;
        String options;
        Scanner scanner = new Scanner(this.requests);
        scanner.useDelimiter("--options|--nextRequest");
        while(scanner.hasNext()) {
            request = scanner.next();
            options = scanner.next();
            requestMap.put(request, options);
        }
    }

    private void checkResponseOptions(String options, String request, RawHttpResponse response) throws IOException {
        Scanner scanner = new Scanner(options);
        scanner.useDelimiter("\n|:|\r\n");
        while(scanner.hasNext()){
            String option = scanner.next();
            if(option.equalsIgnoreCase("AssertHeader")){
                String headerField  = scanner.next();
                String headerFieldValue = scanner.next();
                assertEquals(headerFieldValue,
                        response.getHeaders().getFirst(headerField).orElse(""), request, response, headerField);
            } else if(option.equalsIgnoreCase("AssertStatusCode")){
                String statusCode = scanner.next();
                assertEquals(statusCode, Integer.valueOf(response.getStatusCode()).toString(), request, response, "Statuscode");
            } else if(option.equalsIgnoreCase("BodyContains")){
                String regexString = scanner.next();
                assertBodyContains(regexString, request, response);
            }
        }
    }

    private void assertEquals(String s, String orElse, String request, RawHttpResponse response, String headerField) {
        if(!s.equalsIgnoreCase(orElse)){
            String errMsg = ". Error: " + headerField + " was expected to be '" + s + "' but was: '" + orElse + "'\n\n=========================" +
                    "==============================";
            noLogs++;
            loggedErrors.put(noLogs.toString() + errMsg + "\n\n",  noLogs.toString() + ". Request: \n\n" + request.toString() + "\n\n",
                    noLogs.toString() + ". Response: \n\n" + response.toString() + "\n\n=========================" +
                            "===============================\n\n");
        }
    }

    private void assertBodyContains(String regexString, String request, RawHttpResponse response) throws IOException {
        String body = response.eagerly().getBody().toString();
        Pattern regexPattern = Pattern.compile(regexString);
        Matcher matcher = regexPattern.matcher(body);
        boolean matches = matcher.find();
        System.out.println("Regex Pattern matched: " + matches);
        if(!matches){
            String errMsg = ". Error: RegexString '" + regexString + "' did not match in HTTP-Response-Body\n\n===========================" +
                    "==============================";
            noLogs++;
            loggedErrors.put(noLogs.toString() + errMsg + "\n\n",  noLogs.toString() + ". Request: \n\n" + request.toString() + "\n\n",
                    noLogs.toString() + ". Response-Body: \n\n" + body + "\n\n=========================" +
                            "===============================\n\n");
        }
    }

    private void saveResults(){
        Path path = Paths.get("results_" + getCurrentTimeStamp() + ".txt");
        try{
            Files.createFile(path);
            Files.write(path, loggedErrors.toString().getBytes(), StandardOpenOption.APPEND);
            System.exit(noLogs);
        } catch (IOException e){
            System.out.println("An Exception occured when trying to write File: " + path.toString());
            System.out.println("ErrMsg: " +  e.getMessage() + "\n");
            System.out.println("Results printed to Console because File Operation Failed! \n\n");
            System.out.println(loggedErrors.toString());
            System.exit(noLogs);
        }
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
