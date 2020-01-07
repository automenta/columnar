package columnar

import kotlinx.coroutines.flow.*

/**
 * semigroup
 */
typealias Vect0r<T> = Pai2<() -> Int, (Int) -> T>

val <T>   Vect0r<T>.size get() = first
/*
val <T, O : Vect0r<T>>  O.first
    get() = this::size
val <T, O : Vect0r<T>>  O.second
    get() = this::get
*/

typealias Matrix<T> = Pai2<
        /**shape*/
        IntArray,
        /**accessor*/
            (IntArray) -> T>

operator fun <T> Matrix<T>.get(vararg c: Int): T = second(c)
//val <T> Vect0r<T>.size: Int get() = first.invoke()
infix fun <O, R, F : (O) -> R> O.`→`(f: F) = this.let(f)

operator fun <A, B, R, O : (A) -> B, G : (B) -> R> O.times(b: G) = { a: A -> a `→` this `→` (b) }
infix fun <A, B, R, O : (A) -> B, G : (B) -> R> O.`→`(b: G) = this * b
/**
 * G follows F
 */
infix fun <A, B, C, G : (B) -> C, F : (A) -> B> G.`⚬`(f: F) = { a: A -> a `→` f `→` this }

/**
 * (λx.M[x]) → (λy.M[y])	α-conversion
 * https://en.wikipedia.org/wiki/Lambda_calculus
 * */
inline infix fun <reified A, C, B : (A) -> C, V : Vect0r<A>, R : Vect0r<C>> V.α(m: B): Vect0r<C> = map(m)

fun <A, C, B : (A) -> C, T : Iterable<A>> T.α(m: B) = this.map { it `→` m }
fun <A, C, B : (A) -> C, T : Sequence<A>> T.α(m: B) = this.map { it `→` m }
fun <A, C, B : (A) -> C, T : Flow<A>> T.α(m: B) = this.map { it `→` m }
infix fun <A, C, B : (A) -> C> List<A>.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }
fun <A, C, B : (A) -> C> Array<A>.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }
fun <C, B : (Int) -> C> IntArray.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }
fun <C, B : (Float) -> C> FloatArray.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }
fun <C, B : (Double) -> C> DoubleArray.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }
fun <C, B : (Long) -> C> LongArray.α(m: B): Vect0r<C> = Vect0r({ this.size }) { i: Int -> this.get(i) `→` m }

/**right identity*/
val <T : Any?> T.`⟲` get() = { this }

/**right identity*/
infix fun <T, R, F : (T) -> R> T.`⟲`(f: F) = { f(this) }

@JvmName("vlike_Sequence_1")
inline operator fun <reified T> Sequence<T>.get(vararg index: Int) = get(index)

@JvmName("vlike_Sequence_Iterable2")
inline operator fun <reified T> Sequence<T>.get(indexes: Iterable<Int>) = this[indexes.toList().toIntArray()]

@JvmName("vlike_Sequence_IntArray3")
inline operator fun <reified T> Sequence<T>.get(index: IntArray) = this.toList()[index].asSequence()

@JvmName("vlike_Flow_1")
suspend inline fun <reified T> Flow<T>.get(vararg index: Int) = get(index)

@Suppress("USELESS_CAST")
@JvmName("vlike_Flow_Iterable2")
suspend inline fun <reified T> Flow<T>.get(indexes: Iterable<Int>) = this.get(indexes.toList().toIntArray() as IntArray)

@JvmName("vlike_Flow_IntArray3")
suspend inline fun <reified T> Flow<T>.get(index: IntArray) = this.toList()[index].asFlow()

@JvmName("vlike_List_1")
inline operator fun <reified T> List<T>.get(vararg index: Int) = get(index)

@JvmName("vlike_List_Iterable2")
inline operator fun <reified T> List<T>.get(indexes: Iterable<Int>) = this[indexes.toList().toIntArray()]

@JvmName("vlike_List_IntArray3")
inline operator fun <reified T> List<T>.get(index: IntArray) = List(index.size) { i: Int -> this[index[i]] }

@JvmName("vlike_Array_1")
inline operator fun <reified T> Array<T>.get(vararg index: Int) = get(index)

@JvmName("vlike_Array_Iterable2")
inline operator fun <reified T> Array<T>.get(indexes: Iterable<Int>) = this[indexes.toList().toIntArray()]

@JvmName("vlike_Array_IntArray3")
inline operator fun <reified T> Array<T>.get(index: IntArray) = Array(index.size) { i: Int -> this[index[i]] }

@JvmName("vlike_Vect0r_get")
inline operator fun <reified T> Vect0r<T>.get(index: Int): T = second(index)

@JvmName("vlike_Vect0r_1")
inline operator fun <reified T> Vect0r<T>.get(vararg index: Int): Vect0r<T> = get(index)

@JvmName("vlike_Vect0r_Iterable2")
inline operator fun <reified T> Vect0r<T>.get(indexes: Iterable<Int>): Vect0r<T> = this[indexes.toList().toIntArray()]

@JvmName("vlike_Vect0r_IntArray3")
operator fun <T> Vect0r<T>.get(index: IntArray): Vect0r<T> = Vect0r(index.size.`⟲`, { ix: Int -> second(index[ix]) })

inline fun <reified T> Vect0r<T>.toArray() = this.let { (_, vf) -> Array(size()) { vf(it) } }
inline fun <reified T> Vect0r<T>.toList():List<T> = object : AbstractList<T>() {
    override val size get() = size()
    override operator fun get(index: Int): T = second(index)
}

fun <T> Vect0r<T>.toSequence() = this.let { (size, vf) ->
    sequence {
        for (ix in 0 until size())
            yield(vf(ix))
    }
}

fun <T> Vect0r<T>.toFlow() = this.let { (_, vf) ->
    flow {
        for (ix in 0 until size())
            emit(vf(ix))
    }
}

fun <T, R, V : Vect0r<T>> V.map(fn: (T) -> R): Vect0r<R> = Vect0r(size, { ix: Int -> second(ix).let(fn) })

inline fun <reified T, R> Vect0r<T>.mapIndexed(fn: (Int, T) -> R) = List(size()) { ix -> fn(ix, this[ix]) }
inline fun <reified T> Vect0r<T>.forEach(fn: (T) -> Unit) {
    for (ix in (0 until size())) fn(this[ix])
}

inline fun <reified T, R> Vect0r<T>.forEachIndexed(fn: (Int, T) -> Unit) {
    for (ix in (0 until size())) fn(ix, this[ix])
}

fun <T> vect0rOf(vararg a: T): Vect0r<T> = Vect0r({ a.size }, { a.get(it) })
/**
 * Returns a list of pairs built from the elements of `this` array and the [other] array with the same index.
 * The returned list has length of the shortest collection.
 *
 * @sample samples.collections.Iterables.Operations.zipIterable
 */
inline infix fun <T, reified R> List<T>.zip(other: Vect0r<R>): List<Pai2<T, R>> {
    return zip(other.toArray()) { t1, t2 -> t1 t2 t2 }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T, reified O, P : Pai2<T, O>, R : Vect0r<P>> Vect0r<T>.zip(o: Vect0r<O>): Vect0r<P> =
    Vect0r(this.first) { i: Int -> (this[i] t2 o[i]) as P } as R

fun <T> Array<T>.toVect0r(): Vect0r<T> = Vect0r(size.`⟲`, { ix: Int -> this[ix] })
fun <T> List<T>.toVect0r(): Vect0r<T> = Vect0r(size.`⟲`, { ix: Int -> this[ix] })
suspend fun <T> Flow<T>.toVect0r(): Vect0r<T> = this.toList().toVect0r()
fun <T> Iterable<T>.toVect0r(): Vect0r<T> = this.toList().toVect0r()
fun <T> Sequence<T>.toVect0r(): Vect0r<T> = this.toList().toVect0r()

@JvmName("combine_Flow")
inline fun <reified T> combine(vararg s: Flow<T>) = flow {
    for (f in s) {
        f.collect {
            emit(it)
        }
    }
}

@JvmName("combine_Sequence")
inline fun <reified T> combine(vararg s: Sequence<T>) = sequence {
    for (sequence in s) {
        for (t in sequence) {
            yield(t)
        }
    }
}

@JvmName("combine_List")
inline fun <reified T> combine(vararg a: List<T>) =
    a.sumBy { it.size }.let { size ->
        var x = 0
        var y = 0
        List(size) { i ->
            if (y >= a[x].size) {
                ++x
                y = 0
            }
            a[x][y++]
        }
    }

@JvmName("combine_Vect0r")
inline fun <reified T> combine(vararg vargs: Vect0r<T>): Vect0r<T> = vargs `→` { vargsIn ->
    vargsIn.asIterable().foldIndexed(0 to IntArray(vargsIn.size)) { vix, (acc, avec), vec ->
        acc.plus(vec.size()) `→` { size -> size to avec.also { avec[vix] = size } }
    } `→` { (acc, order) ->
        Vect0r(acc.`⟲`) { ix ->
            order.binarySearch(ix) `→` { offset ->
                (if (0 > offset) 0 - (offset + 1) else offset + 1) `→` { slot ->
                    order[slot] `→` { upperBound ->
                        (if (slot > 0) order[slot - 1] else 0) `→` { beginRange ->
                            vargsIn[slot][ix.rem(upperBound) - beginRange]
                        }
                    }
                }
            }
        }
    }
}

@JvmName("combine_Array")
inline fun <reified T> combine(vararg a: Array<T>) = a.sumBy { it.size }.let { size ->
    var x = 0
    var y = 0; Array(size) { i ->
    if (y >= a[x].size) {
        ++x; y = 0
    }; a[x][y++]
}
}

fun vZipWithNext(src: IntArray) = Vect0r({ src.size / 2 }
) { i: Int ->
    var c = (i * 2)
    IntArray(2) { src[c.also { c++ }] }
}


//array-like mapped map
inline operator fun <reified K, reified V> Map<K, V>.get(ks: Vect0r<K>) = this.get(*ks.toList().toTypedArray())

inline operator fun <reified K, reified V> Map<K, V>.get(ks: Iterable<K>) = this.get(*ks.toList().toTypedArray())
inline operator fun <K, reified V> Map<K, V>.get(vararg ks: K) = Array(ks.size) { ix -> ks[ix].let(this::get)!! }
