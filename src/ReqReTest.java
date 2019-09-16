import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

import java.io.IOException;



public class ReqReTest {
    private static final String testRequest = "GET https://rwmpublish.tstux.r-itservices.at/ HTTP/1.1\n"+
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0\n"+
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"+
            "Accept-Language: de,en-US;q=0.7,en;q=0.3\n"+
            "Connection: keep-alive\n"+
            "Upgrade-Insecure-Requests: 1\n"+
            "Authorization: Basic RVJVMTUyMDIyMDY6OTYwe093MXVQdzAw\n"+
            "Host: rwmpublish.tstux.r-itservices.at";

    private static rawhttp.core.RawHttpResponse sendRequest(String rawRequest) throws IOException {
        TcpRawHttpClient client = new TcpRawHttpClient();
        RawHttp http = new RawHttp();
        RawHttpRequest request = http.parseRequest(rawRequest);
        RawHttpResponse<?> response = client.send(request);
        return response;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(sendRequest(testRequest).toString());
    }

}
