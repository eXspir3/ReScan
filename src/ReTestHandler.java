import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class ReTestHandler {
    private Map<String, String> requestMap = new HashMap<>();
    private File file;
    private Table<String, String, String> loggedErrors = HashBasedTable.create();

    public ReTestHandler(File file) throws FileNotFoundException {
    this.file = file;
    this.importRequests();
    }

    void sendAndTest() throws IOException {
        TcpRawHttpClient client = new TcpRawHttpClient();
        RawHttp http = new RawHttp();
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request);
            testResponse(entry.getValue(), entry.getKey(), response);
        }
    }

    private void importRequests() throws FileNotFoundException {
        String request;
        String options;
        Scanner scanner = new Scanner(this.file);
        scanner.useDelimiter("--options|--nextRequest");
        while(scanner.hasNext()) {
            request = scanner.next();
            options = scanner.next();
            requestMap.put(request, options);
        }
        System.out.println(requestMap.toString());
    }

    private void testResponse(String options, String request, RawHttpResponse response){
        System.out.println(options);
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
            } else if(option.equalsIgnoreCase("ContainRegex")){
                scanner.useDelimiter("\\^|\\$");
                System.out.println("ContainsRegex Option set: " + scanner.next());
                scanner.useDelimiter("\n|:|\r\n");
            }
        }
        System.out.println(loggedErrors.toString());
    }

    private void assertEquals(String s, String orElse, String request, RawHttpResponse response, String headerField) {
        if(!s.equalsIgnoreCase(orElse)){
            String errMsg = headerField + " was expected to be '" + s + "' but was: '" + orElse + "'";
            loggedErrors.put(errMsg, request, response.toString());
        }
    }

    private void assertBodyContains(){

    }


}
