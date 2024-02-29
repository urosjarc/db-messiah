package com.urosjarc.dbmessiah

import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.reflect.KCallable
import kotlin.system.measureNanoTime

class Test_Benchmarks {

    val numCycles = 2

    fun contract_to_method(testContract: Test_Contract): Map<KCallable<*>, KCallable<*>> {
        val map = mutableMapOf<KCallable<*>, KCallable<*>>()
        Test_Contract::class.members
            .filter { it.name.startsWith("test") }
            .forEach { conFun ->
                map[conFun] = testContract::class.members.first { it.name == conFun.name }
            }
        return map
    }

    fun benchmarkCycle(testContract: Test_Contract) {
        val testCycles = mutableListOf<Float>()
        val testMap = this.contract_to_method(testContract = testContract)
        val test = testContract::class.simpleName.toString()

        repeat(numCycles) {
            testMap.forEach { t, u ->
                testContract.prepare()
                val msTime = measureNanoTime { u.call(testContract) } / 100000f
                testCycles.add(msTime)
            }
        }

        val average = testCycles.sum() / testCycles.size
        val errors = testCycles.map { (it - average).absoluteValue }
        val averageError = errors.sum() / errors.size

        println("$test: (${average.toInt()} +- ${averageError.toInt()}) ms")
    }


    @Test
    fun db2() {
        Test_Db2.init()
        this.benchmarkCycle(Test_Db2())
    }

    @Test
    fun derby() {
        Test_Derby.init()
        this.benchmarkCycle(Test_Derby())
    }

    @Test
    fun h2() {
        Test_H2.init()
        this.benchmarkCycle(Test_H2())
    }

    @Test
    fun maria() {
        Test_Maria.init()
        this.benchmarkCycle(Test_Maria())
    }

    @Test
    fun mssql() {
        Test_Mssql.init()
        this.benchmarkCycle(Test_Mssql())
    }

    @Test
    fun mysql() {
        Test_Mysql.init()
        this.benchmarkCycle(Test_Mysql())
    }

    @Test
    fun postgresql() {
        Test_Postgresql.init()
        this.benchmarkCycle(Test_Postgresql())
    }

    @Test
    fun sqlite() {
        Test_Sqlite.init()
        this.benchmarkCycle(Test_Sqlite())
    }

    @Test
    fun oracle() {
        Test_Oracle.init()
        this.benchmarkCycle(Test_Oracle())
    }
}
