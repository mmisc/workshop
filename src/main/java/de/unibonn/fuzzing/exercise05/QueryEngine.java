package de.unibonn.fuzzing.exercise05;

/**
 * Exercise 05: A toy SQL-like query engine.
 *
 * Supported grammar (loosely):
 *
 *   SELECT <cols> FROM <table>
 *   INSERT INTO <table> VALUES ( <vals> )
 *   UPDATE <table> SET <col>=<val> WHERE <cond>
 *   DELETE FROM <table> WHERE <cond>
 *   CREATE TABLE <table> ( <cols> )
 *
 * The parser rejects anything that does not start with one of the
 * keywords above. Guessing SELECT from random bytes takes the fuzzer
 * a long time without a dictionary.
 *
 * This class contains a bug that is only reachable deep in the
 * CREATE TABLE path. You will not reach it in any reasonable time
 * without providing a dictionary as a seed.
 */
public final class QueryEngine {

    private QueryEngine() {
        // utility class
    }

    public static String execute(String query) {
        if (query == null) {
            return "";
        }
        String trimmed = query.trim();
        String upper = trimmed.toUpperCase();

        if (upper.startsWith("SELECT ")) {
            return handleSelect(trimmed.substring("SELECT ".length()));
        } else if (upper.startsWith("INSERT INTO ")) {
            return handleInsert(trimmed.substring("INSERT INTO ".length()));
        } else if (upper.startsWith("UPDATE ")) {
            return handleUpdate(trimmed.substring("UPDATE ".length()));
        } else if (upper.startsWith("DELETE FROM ")) {
            return handleDelete(trimmed.substring("DELETE FROM ".length()));
        } else if (upper.startsWith("CREATE TABLE ")) {
            return handleCreate(trimmed.substring("CREATE TABLE ".length()));
        }
        return "UNKNOWN";
    }

    private static String handleSelect(String rest) {
        int fromIdx = rest.toUpperCase().indexOf(" FROM ");
        if (fromIdx < 0) {
            return "NO_FROM";
        }
        return "SELECTED";
    }

    private static String handleInsert(String rest) {
        int valuesIdx = rest.toUpperCase().indexOf(" VALUES ");
        if (valuesIdx < 0) {
            return "NO_VALUES";
        }
        return "INSERTED";
    }

    private static String handleUpdate(String rest) {
        int setIdx = rest.toUpperCase().indexOf(" SET ");
        int whereIdx = rest.toUpperCase().indexOf(" WHERE ");
        if (setIdx < 0 || whereIdx < 0 || whereIdx < setIdx) {
            return "MALFORMED_UPDATE";
        }
        return "UPDATED";
    }

    private static String handleDelete(String rest) {
        int whereIdx = rest.toUpperCase().indexOf(" WHERE ");
        if (whereIdx < 0) {
            return "DELETE_ALL_REJECTED";
        }
        return "DELETED";
    }

    private static String handleCreate(String rest) {
        int open = rest.indexOf('(');
        int close = rest.lastIndexOf(')');
        if (open < 0 || close < 0 || close < open) {
            return "MALFORMED_CREATE";
        }
        String cols = rest.substring(open + 1, close).trim();
        if (cols.isEmpty()) {
            return "EMPTY_COLS";
        }
        // Planted bug: assumes every comma-separated column has a
        // type after a space. Inputs like "(id,name)" slip through
        // and trigger ArrayIndexOutOfBoundsException below.
        String[] colDefs = cols.split(",");
        for (String def : colDefs) {
            String[] nameAndType = def.trim().split(" ");
            String name = nameAndType[0];
            String type = nameAndType[1];
            if (type.equalsIgnoreCase("BLOB")) {
                return "BLOBS_NOT_SUPPORTED";
            }
        }
        return "CREATED";
    }
}
