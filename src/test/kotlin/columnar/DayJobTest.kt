package columnar


import columnar.IOMemento.*
import columnar.context.Columnar
import columnar.context.FixedWidth
import columnar.context.NioMMap
import columnar.context.RowMajor
import columnar.context.RowMajor.Companion.fixedWidthOf
import columnar.context.RowMajor.Companion.indexableOf
import kotlin.system.measureTimeMillis

class DayJobTest/* : StringSpec()*/ {

    //        val suffix = "_100"//"_RD"  105340
//        val suffix = "_1000"//"_RD"  3392440
//        val suffix = "_10000"     //"_RD"  139618738 writeop took 231874ms

    //    val suffix = "_100000"     //"_RD"
//      val suffix = "_1000000"     //"_RD"
//    val suffix = "_500000"     //"_RD"
    //    val suffix = "_300000"     //"_RD"
    //    val suffix = "_400000"     //"_RD"
    //    val suffix = "_1000"//"_RD"
    //    val suffix = "_RD"
    //    val suffix = "_1000"//"_RD"
    //    val suffix = "_1000"//"_RD"
    //    val suffix = "_1000"//"_RD"
        val suffix = ""
    val s = "/vol/aux/rejuve/rejuvesinceapril2019" + suffix + ".fwf"
    val coords = intArrayOf(
        0, 11,
        11, 15,
        15, 25,
        25, 40,
        40, 60,
        60, 82,
        82, 103,
        103, 108
    ).zipWithNext() ///.map<Pai2<Int, Int>, Tw1nt, Vect0r<Pai2<Int, Int>>> { (a,b): Pai2<Int, Int> -> Tw1n (a,b)  /*not fail*/ }/*.map { ints: IntArray -> Tw1nt(ints)  /*not fail*/ } */ /*.map(::Tw1nt) fail */ /* α ::Tw1nt fail*/

    val drivers = vect0rOf(
        IoString as TypeMemento,
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
    val fixedWidth: FixedWidth = fixedWidthOf(nioMMap, coords as Vect02<Int, Int>)
    val indexable = indexableOf(nioMMap, fixedWidth)
    val curs = cursorOf(RowMajor().fromFwf(fixedWidth, indexable, nioMMap, columnar)).also {
        System.err.println("record count=" + it.first)
    }


    var lastmessage: String? = null

    @org.junit.jupiter.api.Test
    fun `pivot+pgroup+reduce`() {
        val filtered = curs[2, 1, 3, 5].resample(0).pivot(
            intArrayOf(0),
            intArrayOf(1, 2),
            intArrayOf(3)
        ).group(intArrayOf(0), floatSum)
/*
        lateinit var second: RowVec
*/
/*        println(
            "row 2 seektime: " +
                    measureTimeMillis {
                        second = filtered.second(2)
                    } + " ms @ " + second.first + " columns"
        )*/
        lateinit var message: String
        val createTempFile = createTempFile("dayjob",".bin")
        System.err.println("writing bin to "+createTempFile.toURI())
        println("writeop took " + measureTimeMillis {
            filtered.writeBinary(createTempFile.absolutePath)
//            second.let {
//                println("row 2 is:")
//                message = stringOf(it)
//            }
        } + "ms")
//        println(message)
//            lastmessage?.shouldBe(  message )
//            lastmessage=message
    }

    @org.junit.jupiter.api.Test
    fun `pivot+group+reduce`() {
        val piv: Cursor = curs[2, 1, 3, 5].resample(0).pivot(
            intArrayOf(0),
            intArrayOf(1, 2),
            intArrayOf(3)
        ).group((0))
        val filtered = join(piv[0], (piv[1 until piv.scalars.first] /*α floatFillNa(0f)*/).`∑`(floatSum))

        lateinit var second: RowVec
        println(
            "row 2 seektime: " +
                    measureTimeMillis {
                        second = filtered.second(2)

                    } + " ms @ " + second.first + " columns"

        )
        lateinit var message: String
        println("row 2 took " + measureTimeMillis {
            second.let {
                println("row 2 is:")
                message = stringOf(it)
            }
        } + "ms")
        println(message)


    }
}

