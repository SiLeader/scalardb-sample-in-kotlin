package net.sileader.scalar.db.test

import com.google.inject.Guice
import com.scalar.database.api.Get
import com.scalar.database.api.Put
import com.scalar.database.config.DatabaseConfig
import com.scalar.database.io.IntValue
import com.scalar.database.io.Key
import com.scalar.database.io.TextValue
import com.scalar.database.service.TransactionModule
import com.scalar.database.service.TransactionService


class ElectronicMoneyWithTransaction : ElectronicMoney() {
    private val mService: TransactionService

    init {
        val injector = Guice.createInjector(TransactionModule(DatabaseConfig(mProps)))
        mService = injector.getInstance(TransactionService::class.java)
        mService.with(NAMESPACE, TABLENAME)
    }

    override fun charge(id: String, amount: Int) {
        val tx = mService.start() // start transaction

        val get = Get(Key(TextValue(ID, id)))
        val result = tx.get(get)

        var balance = amount
        if(result.isPresent) {
            val current = (result.get().getValue(BALANCE).get() as IntValue).get()
            balance += current
        }

        val put = Put(Key(TextValue(ID, id))).withValue(IntValue(BALANCE, balance))
        tx.put(put)

        tx.commit() // commit the transaction (records are automatically recovered in case of failure)
    }

    override fun pay(fromId: String, toId: String, amount: Int) {
        val tx = mService.start()

        // Retrieve the current balances for ids
        val fromGet = Get(Key(TextValue(ElectronicMoney.ID, fromId)))
        val toGet = Get(Key(TextValue(ElectronicMoney.ID, toId)))
        val fromResult = tx.get(fromGet)
        val toResult = tx.get(toGet)

        // Calculate the balances (it assumes that both accounts exist)
        val newFromBalance = (fromResult.get().getValue(ElectronicMoney.BALANCE).get() as IntValue).get() - amount
        val newToBalance = (toResult.get().getValue(ElectronicMoney.BALANCE).get() as IntValue).get() + amount
        if (newFromBalance < 0) {
            throw RuntimeException("$fromId doesn't have enough balance.")
        }

        // Update the balances
        val fromPut = Put(Key(TextValue(ElectronicMoney.ID, fromId)))
            .withValue(IntValue(ElectronicMoney.BALANCE, newFromBalance))
        val toPut = Put(Key(TextValue(ElectronicMoney.ID, toId))).withValue(IntValue(ElectronicMoney.BALANCE, newToBalance))
        tx.put(fromPut)
        tx.put(toPut)

        tx.commit() // Commit the transaction (records are automatically recovered in case of failure)
    }

    override fun close() {
        mService.close()
    }
}
