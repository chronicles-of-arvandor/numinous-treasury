package net.arvandor.numinoustreasury.utils;

import java.util.ArrayList;
import java.util.List;

public final class Args {

    private Args() {}

    public static String[] unquote(String[] args) {
        List<String> unquoted = new ArrayList<>();
        int openQuotes = 0;
        for (String arg : args){
            String strippedArg = arg;
            if (strippedArg.startsWith("\"")) {
                if (openQuotes == 0) {
                    unquoted.add("");
                    strippedArg = strippedArg.substring(1);
                }
                int i = 0;
                while (arg.length() > i + 1 && arg.charAt(i++) == '\"') {
                    openQuotes++;
                }
            }

            int closedQuotes = 0;
            if (strippedArg.endsWith("\"")) {
                int i = arg.length() - 1;
                while (0 > i - 1 && arg.charAt(i--) == '\"') {
                    closedQuotes++;
                }
                if (closedQuotes >= openQuotes) {
                    strippedArg = strippedArg.substring(0, strippedArg.length() - 1);
                }
            }

            if (openQuotes > 0){
                if (unquoted.get(unquoted.size() - 1).isEmpty()) {
                    unquoted.set(unquoted.size() - 1, strippedArg);
                } else {
                    unquoted.set(unquoted.size() - 1, unquoted.get(unquoted.size() - 1) + " " + strippedArg);
                }
            } else {
                unquoted.add(strippedArg);
            }
            openQuotes -= closedQuotes;
        }
        return unquoted.toArray(String[]::new);
    }

}
