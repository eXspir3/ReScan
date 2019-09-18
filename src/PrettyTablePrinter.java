import com.google.common.collect.Table;

import java.util.Iterator;
import java.util.Map;

class PrettyTablePrinter {
    private Table table;

    PrettyTablePrinter(Table table) {
        this.table = table;
    }

    String prettyPrintTable() {
        StringBuilder mapAsString = new StringBuilder("Results: \n\n");
        Iterator iterRow = table.rowMap().entrySet().iterator();
        Iterator iterCol = table.columnMap().entrySet().iterator();
        while (iterRow.hasNext()) {
            Map.Entry entryRow = (Map.Entry) iterRow.next();
            mapAsString.append(entryRow.getKey());
            if (iterCol.hasNext()) {
                Map.Entry entryCol = (Map.Entry) iterCol.next();
                mapAsString.append(entryCol.getKey());
                mapAsString.append(entryCol.getValue().toString().substring(3, entryCol.getValue().toString().length() - 1));
            }
        }
        return mapAsString.toString();
    }
}
