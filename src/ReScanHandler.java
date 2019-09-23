import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.client.TcpRawHttpClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReScanHandler {
    private Map<String, String> requestMap;
    private Path requestsFile;
    private TcpRawHttpClient client;
    private RawHttp http;
    private Table<Integer, String, String> loggedFailed;
    private PrettyTablePrinter prettyTablePrinter;
    private Table<Integer, String, String> loggedPassed;
    private Integer noLogsFailed;
    private Integer noLogsPassed;
    private boolean needsEncryption  = false;
    private boolean needsDecryption  = false;
    private Path privKey;
    private Path pubKey;
    private Path aesKey;
    private final static boolean keepFile = true;
    private final static boolean deleteFile = false;
    private RSAoverAESHandler rsaOverAesHandler;

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

    ReScanHandler(Path requestsFile, Path privKey, Path pubKey, Path aesKey) throws IOException, GeneralSecurityException {
        this.requestsFile = requestsFile;
        this.client = new TcpRawHttpClient();
        this.http = new RawHttp();
        this.requestMap = new HashMap<>();
        this.loggedFailed = HashBasedTable.create();
        this.loggedPassed = HashBasedTable.create();
        this.noLogsFailed = 0;
        this.noLogsPassed = 0;
        this.prettyTablePrinter = new PrettyTablePrinter(this);
        this.privKey = privKey;
        this.pubKey = pubKey;
        this.aesKey = aesKey;
        if (privKey != null) this.needsDecryption = true;
        if(pubKey != null) this.needsEncryption = true;
        this.rsaOverAesHandler = new RSAoverAESHandler();
        this.importRequests();
    }

    void replayWithAssertions() throws IOException, GeneralSecurityException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            checkResponseOptions(entry.getValue(), entry.getKey(), response);
        }
        saveResults();
    }

    void replayNoAssertions() throws IOException, GeneralSecurityException {
        for(Map.Entry<String, String> entry : requestMap.entrySet()){
            RawHttpRequest request = http.parseRequest(entry.getKey());
            RawHttpResponse<?> response = client.send(request).eagerly();
            prettyTablePrinter.addRequestResponse(request, response);
        }
        saveResults();
    }

    private void importRequests() throws IOException, GeneralSecurityException {
        String request;
        String options;
        Scanner scanner;

        if(needsDecryption){
            /*
             * Decrypt AES-Key and save to File unencrypted
             */

            SecretKey AESKey = rsaOverAesHandler.decryptAESKeyAndLoad(aesKey, privKey, keepFile, deleteFile);

            /*
             * Decrypt the provided File using AESKey
             */

            rsaOverAesHandler.decryptFile(requestsFile, AESKey);
            byte[] fileContent = Files.readAllBytes(Paths.get(requestsFile + ".dec"));
            String decryptedString = new String(fileContent, StandardCharsets.UTF_8);
            Files.deleteIfExists(Paths.get(requestsFile + ".dec"));
            scanner = new Scanner(decryptedString);
        }else{
         scanner = new Scanner(this.requestsFile);
        }
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

    private void saveResults() throws IOException, GeneralSecurityException {
        Path resultsFile = Paths.get("results_" + getCurrentTimeStamp() + ".txt");
        try{
            Files.createFile(resultsFile);
            Files.write(resultsFile, prettyTablePrinter.prettyPrintTable(loggedFailed).getBytes(), StandardOpenOption.APPEND);
            Files.write(resultsFile, prettyTablePrinter.prettyPrintTable(loggedPassed).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e){
            System.out.println("An Exception occurred when trying to write File: " + resultsFile.toString() +
                    "ErrMsg: " +  e.getMessage() + "\n" + "Results printed to Console because File Operation Failed! \n\n");
            System.out.println(prettyTablePrinter.prettyPrintTable(loggedFailed));
            System.out.println(prettyTablePrinter.prettyPrintTable(loggedPassed));
            System.exit(noLogsFailed);
        }
        if(needsEncryption) {
            SecretKey AESKey = rsaOverAesHandler.generateAESandEncryptRSA(pubKey, deleteFile, getCurrentTimeStamp());
            System.out.println("Generating RSA-Encrypted AES-KeyFile");
            System.out.println("Using RSA PublicKey to encrypt AES-KeyFile: " + pubKey.toString());

            /*
             * Encrypt the provided File using AESKey
             */

            System.out.println("Encrypting File: " + resultsFile + " with AES-128 GCM / NOPadding");
            rsaOverAesHandler.encryptFile(resultsFile, AESKey);
            Files.deleteIfExists(resultsFile);
        }
        System.exit(noLogsFailed);
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
