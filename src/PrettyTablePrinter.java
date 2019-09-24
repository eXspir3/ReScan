import com.google.common.collect.Table;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that handles converting output in human - readable formats
 */
class PrettyTablePrinter {
    private ReScanHandler handler;
    private final String seperator = "\n\n===================================" +
            "====================================================\n\n";

    PrettyTablePrinter(ReScanHandler handler) {
        this.handler = handler;
    }

    /**
     * Function Returns a Guava Table without brackets and seperators in pretty human readable format
     * @param table Guava Table to be prettyprinted
     * @return Pretty String - Output of Guava Table
     */
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

    /**
     * Generates the human readable Output when an Assertion was wrong and stores it into a guava table
     * @param s The String (Headervalue) that was asserted
     * @param orElse The String (Headervalue) that actually was recorded
     * @param request The corresponding request
     * @param response The corresponding response
     * @param headerField The Headerfield containing the headervalue (String orElse)
     */
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

    /**
     * Generates the human readable output when an assertion was right and store it into a guava table
     * @param s The String (Headervalue) that was asserted
     * @param request The corresponding Request
     * @param response The corresponding Response
     * @param headerField The Headerfield containing the headervalue (String s)
     */
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

    /**
     * Generates the human readable output when the response body did not contain
     * some String or RegexPattern and stores it into a guava table
     * @param regexString The non-matching RegexString
     * @param request The corresponding Request
     * @param response The Body-checked Response
     */
    void addBodyContainsFailed(String regexString, String request, RawHttpResponse response) throws IOException {
        Integer noLogsFailed = handler.getNoLogsFailed() + 1;
        Table<Integer, String, String> loggedFailed = handler.getLoggedFailed();

        String errMsg = ". Failed: RegexString '" + regexString + "' did not match in HTTP-Response-Body" + seperator;
        loggedFailed.put(noLogsFailed,errMsg + "\n\n" + noLogsFailed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsFailed.toString() + ". Response-Body: \n\n" + response.eagerly().toString() + seperator);

        handler.setNoLogsFailed(noLogsFailed);
        handler.setLoggedFailed(loggedFailed);
    }

    /**
     * Generates the human readable output when the response body did contain
     * some String or RegexPattern and stores it into a guava table
     * @param regexString The matched RegexString
     * @param request The corresponding Request
     * @param response The Body-checked Response
     */
    void addBodyContainsPassed(String regexString, String request, RawHttpResponse response) throws IOException {
        Integer noLogsPassed = handler.getNoLogsPassed() + 1;
        Table<Integer, String, String> loggedPassed = handler.getLoggedPassed();

        String msg = ". Passed: BodyContains: " + regexString + seperator;
        loggedPassed.put(noLogsPassed,msg + "\n\n" + noLogsPassed.toString() + ". Request: \n\n" + request + "\n\n",
                noLogsPassed.toString() + ". Response-Body: \n\n" + response.eagerly().toString() + seperator);

        handler.setNoLogsPassed(noLogsPassed);
        handler.setLoggedPassed(loggedPassed);
    }

    /**
     * Generates the Output(Response with corresponding Request) for Mode 0 (Assertion will Not be Checked)
     * and stores it into a guava table
     * @param request Request to be stored
     * @param response Response to be stored
     */
    void addRequestResponse(RawHttpRequest request, RawHttpResponse response){
        Integer noLogsFailed = handler.getNoLogsFailed() + 1;
        Table<Integer, String, String> loggedFailed = handler.getLoggedFailed();

        loggedFailed.put(noLogsFailed, "\n\n" + noLogsFailed.toString() + ". Request: \n\n" + request.toString(),
                noLogsFailed.toString() + ". Response: \n\n" + response.toString());

        handler.setNoLogsFailed(noLogsFailed);
        handler.setLoggedFailed(loggedFailed);
    }
}