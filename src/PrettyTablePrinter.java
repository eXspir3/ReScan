import com.google.common.collect.Table;
import rawhttp.core.RawHttpResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

class PrettyTablePrinter {
    private ReScanHandler handler;
    private final String seperator = "\n\n===================================" +
            "====================================================\n\n";

    PrettyTablePrinter(ReScanHandler handler) {
        this.handler = handler;
    }

    String prettyPrintTable(Table<Integer, String, String> table) {
        StringBuilder mapAsString = new StringBuilder("Results: \n\n");
        Iterator<Map.Entry<Integer, Map<String, String>>> iterRow = table.rowMap().entrySet().iterator();
        Iterator<Map.Entry<String, Map<Integer, String>>> iterCol = table.columnMap().entrySet().iterator();
        while (iterRow.hasNext()) {
            Map.Entry<Integer, Map<String, String>> entryRow = iterRow.next();
            mapAsString.append(entryRow.getKey());
            if (iterCol.hasNext()) {
                Map.Entry<String, Map<Integer, String>> entryCol = iterCol.next();
                mapAsString.append(entryCol.getKey());
                mapAsString.append(entryCol.getValue().toString().substring(3, entryCol.getValue().toString().length() - 1));
            }
        }
        return mapAsString.toString();
    }

    void addAssertEqualsFailed(String s, String orElse, String request,
                                      RawHttpResponse response, String headerField){
        Integer noLogsFailed = handler.getNoLogsFailed() + 1;
        Table<Integer, String, String> loggedFailed = handler.getLoggedFailed();

        String errMsg = ". Failed: " + headerField + " was expected to be '" + s + "' but was: '" + orElse + "'" + seperator;
        loggedFailed.put(noLogsFailed,errMsg + "\n\n" + noLogsFailed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsFailed.toString() + ". Response: \n\n" + response.toString() + seperator);

        handler.setNoLogsFailed(noLogsFailed);
        handler.setLoggedFailed(loggedFailed);
    }

    void addAssertEqualsPassed(String s, String request,
                               RawHttpResponse response, String headerField){
        Integer noLogsPassed = handler.getNoLogsPassed() + 1;
        Table<Integer, String, String> loggedPassed = handler.getLoggedPassed();

        String msg = ". Passed: AssertHeader: " + headerField + "=" +  s + seperator;
        loggedPassed.put(noLogsPassed, msg + "\n\n" + noLogsPassed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsPassed.toString() + ". Response: \n\n" + response.toString() + seperator);

        handler.setNoLogsPassed(noLogsPassed);
        handler.setLoggedPassed(loggedPassed);
    }

    void addBodyContainsFailed(String regexString, String request, RawHttpResponse response) throws IOException {
        Integer noLogsFailed = handler.getNoLogsFailed() + 1;
        Table<Integer, String, String> loggedFailed = handler.getLoggedFailed();

        String errMsg = ". Failed: RegexString '" + regexString + "' did not match in HTTP-Response-Body" + seperator;
        loggedFailed.put(noLogsFailed,errMsg + "\n\n" + noLogsFailed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsFailed.toString() + ". Response-Body: \n\n" + response.eagerly().toString() + seperator);

        handler.setNoLogsFailed(noLogsFailed);
        handler.setLoggedFailed(loggedFailed);
    }

    void addBodyContainsPassed(String regexString, String request, RawHttpResponse response) throws IOException {
        Integer noLogsPassed = handler.getNoLogsPassed() + 1;
        Table<Integer, String, String> loggedPassed = handler.getLoggedPassed();

        String msg = ". Passed: BodyContains: " + regexString + seperator;
        loggedPassed.put(noLogsPassed,msg + "\n\n" + noLogsPassed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsPassed.toString() + ". Response-Body: \n\n" + response.eagerly().toString() + seperator);

        handler.setNoLogsPassed(noLogsPassed);
        handler.setLoggedPassed(loggedPassed);
    }
}