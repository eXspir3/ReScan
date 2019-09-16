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
    private Map<Integer, RawHttpResponse> testedResponseMap = new HashMap<>();

    void sendRequests() throws IOException {
        TcpRawHttpClient client = new TcpRawHttpClient();
        RawHttp http = new RawHttp();
        for(Map.Entry<String, String> entry :requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getValue());
            RawHttpResponse<?> response = client.send(request);
            testResponse(entry.getKey(), entry.getValue(), response);
        }
    }

    void importRequests(File file) throws FileNotFoundException {
        String request;
        String options;
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("--options|--nextRequest");
        while(scanner.hasNext()) {
            request = scanner.next();
            options = scanner.next();
            requestMap.put(request, options);
        }
    }

    private void testResponse(String options, String Request, RawHttpResponse response){
        Scanner scanner = new Scanner(options);
        scanner.useDelimiter(":|;");
        while(scanner.hasNext()){
            String option = scanner.next();
            if(option.startsWith("AssertHeader:")){
                System.out.println("AssertHeader: ");
                assertEquals(scanner.next(),
                        response.getHeaders().getFirst(scanner.next()).orElse(""));
            } else if(option.startsWith("AssertStatusCode: ")){
                System.out.println("AssertStatusCode");
            }


        }
    }

    private void assertEquals(String s, String orElse) {
        System.out.println("Ist " + s + " dasselbe wie " + orElse + " ?");
    }


}
