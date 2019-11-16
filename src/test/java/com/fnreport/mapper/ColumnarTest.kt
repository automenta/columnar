package com.fnreport.mapper

import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

@UseExperimental(InternalCoroutinesApi::class)

class ColumnarTest : StringSpec() {

    val columns = listOf("date", "channel", "delivered", "ret").zip(
            arrayListOf((0 to 10), (10 to 84), (84 to 124), (124 to 164)).zip(
                    listOf(dateMapper(),
                            stringMapper(),
                            floatMapper(),
                            floatMapper(
                            ))))
    val f20 = FixedRecordLengthFile("src/test/resources/caven20.fwf")
    val f4 = FixedRecordLengthFile("src/test/resources/caven4.fwf")
    val c20 = Columnar(f20, columns)
    val c4 = Columnar(f4, columns)

    override fun beforeTest(testCase: TestCase) {

    }


    init {
        "values" {
            val values20 = decode(1, c20)
            System.err.println(values20)
            val values4 = decode(1, c4)
            System.err.println(values4)
            values20.shouldBe(values4)
        }

        "pivot" {
                        val values4 = decode(1, c4)

            val p4 = spivot(/*c4.pivot(*/c4, intArrayOf  (0), 1, 2, 3)/*)*/
            val x = s4(p4)
            System.err.println(x)
        }

        "group" {
            val p4 = c4.group(listOf(0))

            val x = s4(p4)
            System.err.println(x)
        }
    }

    private suspend fun spivot(c4: Columnar, arrayOf: IntArray, i: Int,vararg rhs:Int ): Columnar {
       return c4.pivot(arrayOf , i,*rhs )
    }

    private suspend fun s4(columnar: Columnar) =
            columnar.values(0).toList().map { it }


    private suspend fun decode(row: Int, columnar: Columnar): List<Any?> {

        return columnar.values(1).first()


    }

}