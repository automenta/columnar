package columnar.context

import columnar.*
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import kotlin.coroutines.CoroutineContext

/**
 * ordering arranges the row and column IO chunking to tailor the io access patterns.
 *
 * The composable options are
 *  * Indexable Addressability Consumer
 *  * linear Addressability provider
 *  * coordinate system translations for such as pivot and inversion
 */
sealed class Ordering : CoroutineContext.Element {
    override val key: CoroutineContext.Key<Ordering> get() = orderingKey
    companion object {
        val orderingKey= object : CoroutineContext.Key<Ordering> {}
    }
}

/**
 * [x++,y,z]
 * [x,y++,z]
 */
class RowMajor : Ordering(){
    companion object{

        //todo: move to rowMajor
        fun fixedWidthOf(
            nio: NioMMap,
            coords: Vect0r<IntArray>,
            defaulteol: () -> Byte = '\n'::toByte
        ) = FixedWidth(recordLen = defaulteol() `→` { endl: Byte ->
            nio.mf.mappedByteBuffer.get().duplicate().clear().run {
                while (get() != endl);
                position()
            }
        }, coords = coords)


        //todo: move to rowMajor
        fun indexableOf(
            nio: NioMMap,
            fixedWidth: FixedWidth,
            mappedByteBuffer: MappedByteBuffer = nio.mf.mappedByteBuffer.get()
        ): Indexable = Indexable(size = (nio.mf.randomAccessFile.length() / fixedWidth.recordLen)::toInt) { recordIndex ->
            val lim = { b: ByteBuffer -> b.limit(fixedWidth.recordLen) }
            val pos = { b: ByteBuffer -> b.position(recordIndex * fixedWidth.recordLen) }
            val sl = { b: ByteBuffer -> b.slice() }
            mappedByteBuffer `⟲` (lim `⚬` sl `⚬` pos)
        }

//todo: move to rowMajor

        fun TableRoot.name(xy: IntArray) = this.let { (_, rootContext) ->
            (rootContext[Arity.arityKey]!! as Columnar).let { cnar ->
                cnar.second!![(rootContext[Ordering.orderingKey]!! as? ColumnMajor)?.let { xy[1] } ?: xy[0]]
            }
        }


        //todo: move to rowMajor
        /**
         * this builds a context and launches a cursor in the given NioMMap frame of reference
         */
        fun fromFwf(
            ordering: Ordering,
            fixedWidth: FixedWidth,
            indexable: Indexable,
            nio: NioMMap,
            columnarArity: Columnar
        ): TableRoot = runBlocking(
            ordering +
                    fixedWidth +
                    indexable +
                    nio +
                    columnarArity
        ) { Pai2(nio.values(), this.coroutineContext) }
    }
}


/**
 * [x,y++]
 * [x++,y]
 */
class ColumnMajor : Ordering()

/**
 * {x,y,z}+-(1|n|n^?)]
 */
class Hilbert : Ordering()

/**for seh
 * Associative||Abstract, Variadic,
 */
class RTree : Ordering()
