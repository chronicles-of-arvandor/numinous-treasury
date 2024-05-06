package net.arvandor.numinoustreasury.utils

object Args {
    fun unquote(args: Array<String>): Array<String> {
        val unquoted: MutableList<String> = ArrayList()
        var openQuotes = 0
        for (arg in args) {
            var strippedArg = arg
            if (strippedArg.startsWith("\"")) {
                if (openQuotes == 0) {
                    unquoted.add("")
                    strippedArg = strippedArg.substring(1)
                }
                var i = 0
                while (arg.length > i + 1 && arg[i++] == '\"') {
                    openQuotes++
                }
            }

            var closedQuotes = 0
            if (strippedArg.endsWith("\"")) {
                var i = arg.length - 1
                while (0 > i - 1 && arg[i--] == '\"') {
                    closedQuotes++
                }
                if (closedQuotes >= openQuotes) {
                    strippedArg = strippedArg.substring(0, strippedArg.length - 1)
                }
            }

            if (openQuotes > 0) {
                if (unquoted[unquoted.size - 1].isEmpty()) {
                    unquoted[unquoted.size - 1] = strippedArg
                } else {
                    unquoted[unquoted.size - 1] = unquoted[unquoted.size - 1] + " " + strippedArg
                }
            } else {
                unquoted.add(strippedArg)
            }
            openQuotes -= closedQuotes
        }
        return unquoted.toTypedArray()
    }
}
