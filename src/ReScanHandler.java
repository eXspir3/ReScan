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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReScanHandler {
    private Map<String, String> requestMap;
    private File requests;
    private TcpRawHttpClient client;
    private RawHttp http;
    private Table<Integer, String, String> loggedFailed;
    private PrettyTablePrinter prettyTablePrinter;

    Table<Integer, String, String> getLoggedFailed() {
        return loggedFailed;
    }

    void setLoggedFailed(Table<Integer, String, String> loggedFailed) {
        this.loggedFailed = loggedFailed;
    }

    Table<Integer, String, String> getLoggedPassed() {
        return loggedPassed;
    }

    void setLoggedPassed(Table<Integer, String, String> loggedPassed) {
        this.loggedPassed = loggedPassed;
    }

    Integer getNoLogsFailed() {
        return noLogsFailed;
    }

    void setNoLogsFailed(Integer noLogsFailed) {
        this.noLogsFailed = noLogsFailed;
    }

    Integer getNoLogsPassed() {
        return noLogsPassed;
    }

    void setNoLogsPassed(Integer noLogsPassed) {
        this.noLogsPassed = noLogsPassed;
    }

    private Table<Integer, String, String> loggedPassed;
    private Integer noLogsFailed;
    private Integer noLogsPassed;

    ReScanHandler(File requests) throws FileNotFoundException {
        this.requests = requests;
        this.client = new TcpRawHttpClient();
        this.http = new RawHttp();
        this.requestMap = new HashMap<>();
        this.loggedFailed = HashBasedTable.create();
        this.loggedPassed = HashBasedTable.create();
        this.noLogsFailed = 0;
        this.noLogsPassed = 0;
        this.prettyTablePrinter = new PrettyTablePrinter(this);
        this.importRequests();
    }

    void replayWithAssertions() throws IOException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            checkResponseOptions(entry.getValue(), entry.getKey(), response);
        }
        saveResults();
    }

    void replayNoAssertions() throws IOException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            noLogsFailed++;
            loggedFailed.put(noLogsFailed, "\n\n" + noLogsFailed.toString() + ". Request: \n\n" + request.toString(),
                    noLogsFailed.toString() + ". Response: \n\n" + response.toString());
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
           prettyTablePrinter.addAssertEqualsFailed(s, orElse, request, response,headerField);
        } else {
           prettyTablePrinter.addAssertEqualsPassed(s, request, response,headerField);
        }
    }

    private void assertBodyContains(String regexString, String request, RawHttpResponse response) throws IOException {
        String body = response.eagerly().getBody().toString();
        Pattern regexPattern = Pattern.compile(regexString);
        Matcher matcher = regexPattern.matcher(body);
        boolean matches = matcher.find();
        if(!matches){
            prettyTablePrinter.addBodyContainsFailed(regexString, request, response);
        } else {
            prettyTablePrinter.addBodyContainsPassed(regexString, request, response);
        }
    }

    private void saveResults(){
        Path path = Paths.get("results_" + getCurrentTimeStamp() + ".txt");
        try{
            Files.createFile(path);
            Files.write(path, prettyTablePrinter.prettyPrintTable(loggedFailed).getBytes(), StandardOpenOption.APPEND);
            Files.write(path, prettyTablePrinter.prettyPrintTable(loggedPassed).getBytes(), StandardOpenOption.APPEND);
            System.exit(noLogsFailed);
        } catch (IOException e){
            System.out.println("An Exception occurred when trying to write File: " + path.toString() +
                    "ErrMsg: " +  e.getMessage() + "\n" + "Results printed to Console because File Operation Failed! \n\n");
            System.out.println(prettyTablePrinter.prettyPrintTable(loggedFailed));
            System.out.println(prettyTablePrinter.prettyPrintTable(loggedPassed));
            System.exit(noLogsFailed);
        }
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
