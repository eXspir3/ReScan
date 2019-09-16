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

public class ReTestHandler {
    private Map<Integer, String> requestMap = new HashMap<Integer, String>();
    private Map<Integer, String> responseMap = new HashMap<Integer, String>();

    public void sendRequests() throws IOException {
        TcpRawHttpClient client = new TcpRawHttpClient();
        RawHttp http = new RawHttp();
        for(Map.Entry<Integer, String> entry :requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getValue());
            RawHttpResponse<?> response = client.send(request);
            System.out.println(response);
        }
    }

    public void importRequests(File file) throws FileNotFoundException {
        Integer i = 0;
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("--nextRequest");
        while(scanner.hasNext()) {
            requestMap.put(i, scanner.next());
            i++;
        }
    }
}
