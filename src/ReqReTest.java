import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

import java.io.File;
import java.io.IOException;



public class ReqReTest {
    private static final String testRequest = "GET https://www.google.at HTTP/1.1\n"+
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0\n"+
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"+
            "Accept-Language: de,en-US;q=0.7,en;q=0.3\n"+
            "Connection: keep-alive\n"+
            "Upgrade-Insecure-Requests: 1\n"+
            "Host: google.at";


    public static void main(String[] args) throws IOException {
        ReTestHandler handler = new ReTestHandler();
        File file = new File("C:\\Entwicklung\\Praktikant\\IntelliJ\\ReqReTest\\Requests.txt");
        handler.importRequests(file);
        //handler.sendRequests();
    }

}
