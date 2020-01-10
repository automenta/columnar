package id.rejuve

import columnar.*
import columnar.IOMemento.*
import columnar.context.*
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class DayJobTest : StringSpec() {

    val suffix = "_100"//"_RD"
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
/*        "resample" {
            val fromFwf = fromFwf(RowMajor(), fixedWidth, indexable, nioMMap, co
lumnar)

            (cursorOf(fromFwf)).let { curs ->
                System.err.println("record count=" + curs.first())
                val scalars = curs.scalars
                val pai2 = scalars α Scalar::pair
                System.err.println("" + pai2.toList())
                System.err.println("" + scalars[1].pair)
                System.err.println("" + curs.toList().first().reify)
                System.err.println("" + curs.toList().first().left)
            }
        }*/
//        "pivot" {
//
//            val curs = cursorOf(fromFwf(RowMajor(), fixedWidth, indexable, nioMMap, columnar))
//            curs.let { curs ->
//                System.err.println("record count=" + curs.first())
//                val sc
//                alars = curs.scalars
//            }
//            val pivot = curs[2, 1, 3, 5].resample(0).pivot(intArrayOf(0), intArrayOf(1, 2), intArrayOf(3))
//
//            System.err.println("" + pivot.scalars.size() + " columns ")
//            System.err.println("" + pivot.scalars.map { scalar: Scalar -> scalar.second }.toList())
//        }
        "pivot+group" {
            logDebug { ("try out -XX:MaxDirectMemorySize=${(nioMMap.mf.randomAccessFile.length() + 100) / 1024 / 1024}m") }
            val curs = cursorOf(fromFwf(RowMajor(), fixedWidth, indexable, nioMMap, columnar))
            curs.let { curs1 ->
                System.err.println("record count=" + curs1.first())
            }
            val piv = curs[2, 1, 3, 5].resample(0).pivot(intArrayOf(0), intArrayOf(1, 2), intArrayOf(3)).group(
                sortedSetOf(0),
                vect0rOf(IoFloat t2 { acc: Any?, lf: Any? ->
                    (((acc as? Float) ?: 0f) + ((lf as? Float) ?: 0f))
                } as GroupReducer))
            logReuseCountdown = 2
            System.err.println("" + piv.scalars.size + " columns ")
            System.err.println("" + piv.scalars.map { scalar: Scalar -> scalar.second }.toList())

            //todo: install bytebuffer as threadlocal
            System.err.println("--- insanity follows")
            launch(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher(),
                CoroutineStart.ATOMIC
            ) {

                val intRange = ((0..piv.size) / Runtime.getRuntime().availableProcessors()).toList().also {
                    System.err.println("using " + it)

                    System.err.println("expecting " + it.map {
                        it.first * fixedWidth.recordLen
                    })
                }.map { span ->
                    async {
                        sequence {
                            for (iy in span) {
                                yield(
                                    piv.second(iy).left.map {
                                        ((it as? Vect0r<*>)?.toList() ?: it).toString().length
                                    }.sum()
                                )
                            }
                        }.sum().toLong()
                    }
                }.awaitAll().sum().also { println("+++++ $it") }
            }.join()
        }
    }
}

