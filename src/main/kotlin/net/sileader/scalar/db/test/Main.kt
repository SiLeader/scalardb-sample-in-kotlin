package net.sileader.scalar.db.test

fun main(args: Array<String>) {
    var mode: String? = null
    var action: String? = null
    var amount = 0
    var to: String? = null
    var from: String? = null

    var i = 0
    while (i < args.size) {
        when {
            "-mode" == args[i] -> mode = args[++i]
            "-action" == args[i] -> action = args[++i]
            "-amount" == args[i] -> amount = Integer.parseInt(args[++i])
            "-to" == args[i] -> to = args[++i]
            "-from" == args[i] -> from = args[++i]
        }
        ++i
    }
    if (mode == null || action == null || to == null || amount < 0) {
        return
    }

    val eMoney = if (mode.equals("storage", ignoreCase = true)) {
        ElectronicMoneyWithStorage()
    } else {
        ElectronicMoneyWithTransaction()
    }

    if (action.equals("charge", ignoreCase = true)) {
        eMoney.charge(to, amount)
    } else if (action.equals("pay", ignoreCase = true)) {
        if (from == null) {
            return
        }
        eMoney.pay(from, to, amount)
    }
    eMoney.close()
}
