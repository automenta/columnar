package id.rejuve

import columnar.*
import columnar.IOMemento.*
import columnar.context.*
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class DayJobTest : StringSpec() {

//    val suffix = "_100"//"_RD"  105340
    //    val suffix = "_1000"//"_RD"  3392440
//val suffix = "_10000"     //"_RD"  139618738
//    val suffix = "_100000"     //"_RD"
    //    val suffix = "_1000"//"_RD"
    val suffix =  "_RD"
//    val suffix = "_1000"//"_RD"
//    val suffix = "_1000"//"_RD"
//    val suffix = "_1000"//"_RD"
    val s = "/vol/aux/rejuve/rejuvesinceapril2019" + suffix + ".fwf"
    val coords = vZipWithNext(
        intArrayOf(
            0, 11,
            11, 15,
            15, 25,
            25, 40,
            40, 60,
            60, 82,
            82, 103,
            103, 108
        )
    )
    val drivers = vect0rOf(
        IoString,
        IoString,
        IoLocalDate,
        IoString,
        IoString,
        IoFloat,
        IoFloat,
        IoString
    )
    val names = vect0rOf(
        "SalesNo",    //        0
        "SalesAreaID",    //    1
        "date",    //           2
        "PluNo",    //          3
        "ItemName",    //       4
        "Quantity",    //       5
        "Amount",    //         6
        "TransMode"    //       7
    )


    val zip = names.zip(drivers)
    val columnar = Columnar.of(zip)
    val nioMMap = NioMMap(MappedFile(s), NioMMap.text(columnar.first))
    val fixedWidth: FixedWidth = fixedWidthOf(nioMMap, coords)
    val indexable = indexableOf(nioMMap, fixedWidth)

    init {

        "pivot+group+reduce" {
            val curs = cursorOf(fromFwf(RowMajor(), fixedWidth, indexable, nioMMap, columnar))
            curs.let { curs1 ->
                System.err.println("record count=" + curs1.first())
            }
            val piv: Cursor = curs[2, 1, 3, 5].resample(0)(floatSum).pivot(
                intArrayOf(0),
                intArrayOf(1, 2),
                intArrayOf(3)
            ).group(0)
            val filtered = join(piv[0], (piv[1 until piv.scalars.size] α floatFillNa(0f))(floatSum))
            lateinit var second:RowVec
            println(
                "row 47 seektime: "+
                        measureTimeMillis {
                    second                     = filtered.second(47)

                }+" ms @ "+            second.size +" columns"


            )
            println("row 47 took "+ measureTimeMillis {

                second.let {
                            println("row 47 is:")
                            println(stringOf(it))
                        }
                    }+"ms")
            filtered.toList().forEach {
                println(stringOf(it))
            }
        }
    }
}

