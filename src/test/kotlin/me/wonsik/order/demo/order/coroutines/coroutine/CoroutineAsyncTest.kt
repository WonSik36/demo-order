package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/4">서스펜딩 함수 활용</a>
 */
class CoroutineAsyncTest : FreeSpec({

    "without async" {
        val time = measureTimeMillis {
            val value1 = getRandomWithDelay(500)
            val value2 = getRandomWithDelay(500)
            println("$value1 + $value2 = ${value1 + value2}")
        }

        println("Take time: $time ms")
        time shouldBeGreaterThanOrEqual 1000
    }

    "async" {
        val time = measureTimeMillis {
            val value1 = async { getRandomWithDelay(500) }
            val value2 = async { getRandomWithDelay(500) }

            println("hello")
            value1.await()
            value2.await()
            println("world")

            println("${value1.await()} + ${value2.await()} = ${value1.await() + value2.await()}")
        }

        println("Take time: $time ms")
        time shouldBeGreaterThanOrEqual 500 shouldBeLessThan 1000
    }

    "async lazy" {
        val time = measureTimeMillis {
            val value1 = async(start = CoroutineStart.LAZY) { getRandomWithDelay(500) }
            val value2 = async(start = CoroutineStart.LAZY) { getRandomWithDelay(500) }

            println("hello")
            value1.start()  // 디스패처에 등록되는거 아닐까?
            value2.start()  // state: New -> Active
            println("world")

            println("${value1.await()} + ${value2.await()} = ${value1.await() + value2.await()}")
        }

        println("Take time: $time ms")
        time shouldBeGreaterThanOrEqual 500 shouldBeLessThan 1000
    }

    "async lazy without start" {
        val time = measureTimeMillis {
            val value1 = async(start = CoroutineStart.LAZY) { getRandomWithDelay(500) }
            val value2 = async(start = CoroutineStart.LAZY) { getRandomWithDelay(500) }

            // 디스패처에 등록 안함
            // value1.start()
            // value2.start()

            println("${value1.await()} + ${value2.await()} = ${value1.await() + value2.await()}")
        }

        println("Take time: $time ms")
        time shouldBeGreaterThanOrEqual 1000
    }

    "async with exception" {
        try {
            coroutineScope {
                val value1 = async {
                    getRandomWithDelayFinally(1000) {   // 같은 자식에도 finally
                        println("getRandomWithDelayFinally cancelled")
                    }
                }
                val value2 = async { throwException(500) }

                try {
                    value1.await()
                    value2.await()  // throw exception
                } catch (ex: IllegalStateException) {
                    throw ex
                } finally { // 부모에도 finally
                    println("scope is cancelled")
                }
            }
        } catch (ex: IllegalStateException) {
            println("coroutineScope is cancelled: $ex")
        }
    }
})

suspend fun getRandomWithDelay(time: Long): Int {
    println("getRandomWithDelay start in thread: ${Thread.currentThread().name}")
    delay(time)
    return Random.nextInt(0, 500)
}

suspend fun getRandomWithDelayFinally(time: Long, block: () -> Unit): Int {
    try {
        println("getRandomWithDelayFinally start in thread: ${Thread.currentThread().name}")
        delay(time)
        return Random.nextInt(0, 500)
    } finally {
        block()
    }
}

suspend fun throwException(time: Long): Int {
    delay(time)
    throw IllegalStateException()
}