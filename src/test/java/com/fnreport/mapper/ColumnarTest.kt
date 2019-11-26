package com.fnreport.mapper

import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.nio.ByteBuffer

val Pair<Int, Int>.size: Int get() = let { (a, b) -> b - a }

typealias RowDecoder = List<Pair<String, Pair<Pair<Int, Int>, (Any?) -> Any?>>>
typealias Table1 = suspend (Int) -> Array<Flow<Any?>>

operator fun Table1.get(vararg reorder: Int): Table1 = { row ->
    val arrayOfFlows = this(row)
    reorder.map { i ->
        flowOf(arrayOfFlows[i].first())
    }.toTypedArray()
}


infix fun RowDecoder.from(rs: RowStore<Flow<ByteBuffer>>): suspend (Int) -> Array<Flow<Any?>> = { row: Int ->
    val cols = this
    val values = rs.values(row)
    val first = lazyOf(values.first())
    first.let { buf ->
        this.mapIndexed { index,
                          (_, convertor) ->
            flowOf(convertor.let { (coords, mapper) ->
                ByteArray(coords.size).also { buf.value.get(it) }.let(mapper)
            })
        }.toTypedArray()
    }
}

@UseExperimental(InternalCoroutinesApi::class)


class ColumnarTest : StringSpec() {

    val columns: List<Pair<String, Pair<Pair<Int, Int>, (Any?) -> Any?>>> = listOf("date", "channel", "delivered", "ret").zip(
            listOf((0 to 10), (10 to 84), (84 to 124), (124 to 164)).zip(
                    listOf(dateMapper,
                            stringMapper,
                            floatMapper,
                            floatMapper)))
    val f20 = FixedRecordLengthFile("src/test/resources/caven20.fwf")
    val f4 = FixedRecordLengthFile("src/test/resources/caven4.fwf")

    val c20 = columns from f20
    val c4 = columns from f4
    val c4remap = c4[0, 0, 0, 1, 3, 2, 1, 1, 1]

    /*
    val c20 = Columnar(f20, columns.toTypedArray())
    val c4 = Columnar(f4, columns.toTypedArray())
*/
    override fun beforeTest(testCase: TestCase) {

    }


    init {

        "dateCol"{
            val any = c20(1)[0].first()
            any.toString().shouldBe("2017-10-22")
            System.err.println(any)

        }
        "size" {
            f4.size.shouldBe(4)
//            c4.size.shouldBe(4)
        }
        "remap"{
            val c41 = c4(1).map { it.first() }
            System.err.println(c41)
            val map = c4remap(1).map { it.first() }
            System.err.println(map)
        }
/*        "values" {
            val values20 = decode(1, c20)
            System.err.println(values20)
            val values4 = decode(1, c4)
            System.err.println(values4)
            values20.shouldBe(values4)
        }

        "pivot" {


            System.err.println("pivot")
            val p4 = c4.pivot(intArrayOf(0), 1, 2, 3)
            System.err.println(p4.columns.map { (s) -> s })
            (0 until p4.size).forEach {

                val values = p4.values(it)
                System.err.println(values)
            }
        }
        "pivotpivot" {


            System.err.println("pivot2")
            val p4 = c4.pivot(intArrayOf(0), 1, 2, 3)
                    .pivot(intArrayOf(), 0,1 , 2, 3)
            System.err.println(p4.columns.map { (s) -> s })
            (0 until p4.size).forEach {

                val values = p4.values(it)
                System.err.println(values)
            }
        }
        "pivotAggregate" {
            val p4 = c4.pivot(*//*listOf(0)*//*intArrayOf(), 1, 2, 3)
            p4[(0 until p4.columns.size) to { any -> any ?: 0 }]

            System.err.println("pivot agg:")
            (0 until p4.size).forEach {
                val values = p4.values(it)
                System.err.println(values)
            }
        }

        "group" {
            val group1 = c4.group((0))
            val group2 = c4.group((1))
            System.err.println("group1:")
            (0 until group1.size).forEach { System.err.println(group1.values(it)) }
            System.err.println("group2:")
            (0 until group2.size).forEach { System.err.println(group2.values(it)) }
            group1[2].values(0).shouldBe(listOf(88.0f))
            group2.values(2)[2].toString().shouldBe(
                    "[4.0, 820.0]"
            )
        }
        "groupAggregate" {
            val sums: List<Int> = listOf(2, 3)
            var group1: Columnar = c4.group((0))
            group1[sums to { any -> any ?: 0f }]
            var group2: Columnar = c4.group((1))
            group2[sums to { any: Any? -> any ?: 0f }]
            System.err.println("group1Agg:")

            (0 until group1.size).map { ind: Int ->
                group1.values(ind).map<Any?, Any> { subject: Any? ->
                    if (subject is List<*> && subject.first() is Float) {
                        (subject as  Collection<Float>).sum()
                    } else "$subject"
                }
            }.forEach { System.err.println("$it") }
        }
        "group+pivot"{
            val by = listOf(0)
            val p4 = c4.pivot(intArrayOf(0), 1, 2, 3)
            val g4 = p4.group((0))
            val res = g4.let { columnar ->
                (0 until columnar.size).map {
                    columnar.values(it)
                }
            }
            System.err.println("pivot+group:")
            val cnames = g4.columns.map { (cname) -> cname }
            res.forEach { row ->
                val tuple = cnames.zip(row)
                System.err.println(tuple)

            }
        }*/
    }
//    private suspend fun decode(row: Int, columnar: Columnar): List<Any?> = columnar.values(row)
}