package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlin.random.Random


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/6">CEH 와 슈퍼 바이저 잡</a>
 */
class CoroutineExceptionTest : FreeSpec({

    "Coroutine Scope" {
        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch (Dispatchers.IO) {
            launch { printRandom() }
        }

        delay(2000L)
    }

    "Coroutine Exception Handler" - {
        "설정하지 않은 경우" {
            shouldThrow<IllegalStateException> {
                val job = coroutineScope {
                    launch { printRandom() }
                    launch { throwException() }
                }

                Unit
            }
        }

        "설정한 경우" {
            val scope = CoroutineScope(Dispatchers.Default)

            val handler = CoroutineExceptionHandler { _, ex ->
                println("Something Happed: $ex")
            }

            val job = scope.launch (Dispatchers.IO + handler) {
                launch { printRandom() }
                launch { throwException() }
            }

            shouldNotThrowAny { job.join() }
        }
    }
})

suspend fun printRandom() {
    println(Thread.currentThread().name)
    delay(1000L)
    println(Random.nextInt(0, 500))
}

suspend fun throwException() {
    delay(500L)

    throw IllegalStateException()
}