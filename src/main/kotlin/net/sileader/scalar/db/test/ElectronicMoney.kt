package net.sileader.scalar.db.test

import java.util.*

abstract class ElectronicMoney {
    companion object {
        const val NAMESPACE = "em"
        const val TABLENAME = "acc"
        const val ID = "id"
        const val BALANCE = "balance"
    }

    protected val mProps: Properties = Properties()

    init {
        mProps.setProperty("scalar.database.contact_points", "localhost")
        mProps.setProperty("scalar.database.username", "cassandra")
        mProps.setProperty("scalar.database.password", "cassandra")
    }

    abstract fun charge(id: String, amount: Int)
    abstract fun pay(fromId: String, toId: String, amount: Int)
    abstract fun close()
}
