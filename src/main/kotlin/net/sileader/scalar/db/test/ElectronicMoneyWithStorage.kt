package net.sileader.scalar.db.test

import com.google.inject.Guice
import com.scalar.database.api.Get
import com.scalar.database.api.Put
import com.scalar.database.config.DatabaseConfig
import com.scalar.database.io.IntValue
import com.scalar.database.io.Key
import com.scalar.database.io.TextValue
import com.scalar.database.service.StorageModule
import com.scalar.database.service.StorageService

class ElectronicMoneyWithStorage : ElectronicMoney() {
    private val mService: StorageService

    init {
        val injector = Guice.createInjector(StorageModule(DatabaseConfig(mProps)))
        mService = injector.getInstance(StorageService::class.java)
        mService.with(NAMESPACE, TABLENAME)
    }

    override fun charge(id: String, amount: Int) {
        val get = Get(Key(TextValue(ID, id)))
        val result = mService.get(get)

        var balance = amount
        if(result.isPresent) {
            val current = (result.get().getValue(BALANCE).get() as IntValue).get()
            balance += current
        }

        val put = Put(Key(TextValue(ID, id))).withValue(IntValue(BALANCE, balance))
        mService.put(put)
    }

    override fun pay(fromId: String, toId: String, amount: Int) {
        val fromGet = Get(Key(TextValue(ID, fromId)))
        val toGet = Get(Key(TextValue(ID, toId)))

        val fromResult = mService.get(fromGet)
        val toResult = mService.get(toGet)

        val newFromBalance = (fromResult.get().getValue(BALANCE).get() as IntValue).get() - amount
        val newToBalance = (toResult.get().getValue(BALANCE).get() as IntValue).get() + amount

        if(newFromBalance < 0) {
            throw RuntimeException("$fromId doesn't have enough balance.")
        }

        val fromPut = Put(Key(TextValue(ID, fromId))).withValue(IntValue(BALANCE, newFromBalance))
        val toPut = Put(Key(TextValue(ID, toId))).withValue(IntValue(BALANCE, newToBalance))

        mService.put(fromPut)
        mService.put(toPut)
    }

    override fun close() {
        mService.close()
    }
}