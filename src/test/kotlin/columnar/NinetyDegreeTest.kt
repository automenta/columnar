package columnar

//import io\.kotlintest\.specs\.StringSpec\n
import columnar.IOMemento.*
import columnar.context.*
import columnar.context.RowMajor.Companion.fixedWidthOf
import columnar.context.RowMajor.Companion.indexableOf
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class NinetyDegreeTest/* : StringSpec()*/ {


    /**
     * wrtie a fixed length networkendian binary fwf of a cursor.
     */
    @org.junit.jupiter.api.Test
    fun rewriteBinFwfRowMajor() {
        val coords = intArrayOf(
            0, 10,
            10, 84,
            84, 124,
            124, 164
        ).zipWithNext()
        val mf = MappedFile("src/test/resources/caven20.fwf")
        val nio = NioMMap(mf)
        val fixedWidth = fixedWidthOf(nio, coords)
        val indexable: Addressable = indexableOf(nio, fixedWidth)
        //we resample and pivot a source cursor
        val piv: Cursor = cursorOf(
            RowMajor().fromFwf(
                fixedWidth, indexable as Indexable, nio, Columnar(
                    vect0rOf(
                        IoLocalDate as TypeMemento,
                        IoString,
                        IoFloat,
                        IoFloat
                    ),
                    vect0rOf<String>("date", "channel", "delivered", "ret")
                )
            )
        ).resample(0).pivot(0.toArray(), 1.toArray(), intArrayOf(2, 3)) α floatFillNa(0f)
        val defaultVarcharSize = 64

        /**
         * open destination file
         */
        val createTempFile = File.createTempFile("ninetyDegreesTest1", ".bin")
        System.err.println("tmpfile is " + createTempFile.toPath())
        piv.writeBinary(createTempFile.absolutePath, defaultVarcharSize)
    }

    @Test
    fun readBinary() {
        val s1 = "ninetyDegreesTest19334425141920886859.bin"
        val s2 = s1 + ".meta"
        val binpath = Paths.get(
            "src",
            "test",
            "resources", s1
        )
        val metapath = Paths.get(
            "src",
            "test",
            "resources", s2
        )

        val cursr = binaryCursor(binpath, metapath)

        System.err.println(cursr.second(0).left.toList())
        System.err.println(cursr.second(1).left.toList())
        System.err.println(cursr.second(2).left.toList())
        System.err.println(cursr.second(3).left.toList())
    }

    @Suppress("USELESS_CAST")
    fun binaryCursor(
        binpath: Path,
        metapath: Path = Paths.get(binpath.toUri().toASCIIString() + ".meta")
    ) = MappedFile(binpath.toString()).use { mf ->
        val lines = Files.readAllLines(metapath)
        lines.removeIf { it.startsWith("# ") || it.isNullOrBlank() }
        val rnames = lines[1].split("\\s+".toRegex()).toVect0r()
        val typeVec = lines[2].split("\\s+".toRegex()).α(IOMemento::valueOf)
        val rcoords: Vect02<Int, Int> = lines[0].split("\\s+".toRegex()).α(String::toInt).zipWithNext()
        val recordlen = rcoords.last().second
        val drivers = NioMMap.binary(typeVec)
        val nio = NioMMap(mf, drivers)
        val fixedWidth = FixedWidth(
            recordlen, rcoords, null.`⟲`, null.`⟲`
        )
        val indexable: Addressable = indexableOf(nio, fixedWidth)
        cursorOf(
            RowMajor().fromFwf(
                fixedWidth,
                indexable as Indexable,
                nio,
                Columnar(
                    typeVec.map {it as TypeMemento }.toArray().toVect0r(),/*solidify the parse*/ rnames
                )
            )
        )
    }
}

