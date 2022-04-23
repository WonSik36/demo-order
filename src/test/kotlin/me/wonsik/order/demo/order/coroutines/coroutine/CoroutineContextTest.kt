package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/5">코루틴 컨텍스트와 디스패처</a>
 */
@DelicateCoroutinesApi
class CoroutineContextTest : FreeSpec({
    "Coroutine Dispatcher" {
        launch {
            printContext("launch 부모의 컨텍스트")
        }.join()

        launch(Job()) {
            printContext("launch with Job 부모의 컨텍스트")
        }.join()

        async {
            printContext("async 부모의 컨텍스트")
        }.join()

        coroutineScope {
            printContext("coroutineScope")
        }


        /* Default */
        launch(Dispatchers.Default) {
            printContext("launch Default")
        }.join()

        async(Dispatchers.Default) {
            printContext("async Default")
        }.await()

        CoroutineScope(Dispatchers.Default).launch {
            printContext("CoroutineScope Default")
        }.join()

        withContext(Dispatchers.Default) {
            printContext("withContext Default")
        }


        /* IO */
        launch(Dispatchers.IO) {
            printContext("launch IO")
        }.join()

        async(Dispatchers.IO) {
            printContext("async IO")
        }.await()

        CoroutineScope(Dispatchers.IO).launch {
            printContext("CoroutineScope IO")
        }.join()

        withContext(Dispatchers.IO) {
            printContext("withContext IO")
        }


        /* Unconfined */
        launch(Dispatchers.Unconfined) {
            printContext("launch Unconfined")
        }.join()

        async(Dispatchers.Unconfined) {
            printContext("async Unconfined")
        }.await()

        CoroutineScope(Dispatchers.Unconfined).launch {
            printContext("CoroutineScope Unconfined")
        }.join()

        withContext(Dispatchers.Unconfined) {
            printContext("withContext Unconfined")
        }


        /* Single Thread */
        launch(newSingleThreadContext("Single Thread 1")) {
            printContext("launch Single Thread 1")
        }.join()

        async(newSingleThreadContext("Single Thread 2")) {
            printContext("async Single Thread 2")
        }.await()

        CoroutineScope(newSingleThreadContext("Single Thread 3")).launch {
            printContext("CoroutineScope Single Thread 3")
        }.join()

        withContext(newSingleThreadContext("Single Thread 4")) {
            printContext("withContext Single Thread 4")
        }
    }

    "부모와의 관계" {
        val job = launch {

            // Not Children
            launch(Job()) {
                printContextWithDelay("launch1", 1000L) {
                    println("1!")
                }
            }

            // Children 1
            launch(Job(coroutineContext[Job])) {
                printContextWithDelay("launch2", 1000L) {
                    println("2!")
                }
            }

            // Children 2
            launch {
                printContextWithDelay("launch3", 1000L) {
                    println("3!")
                }
            }
        }

        delay(500L)
        job.cancelAndJoin()
        delay(1000L)
        // 3!
    }

    "부모는 자식 코루틴의 종료를 기다림" {
        val elapsed = measureTimeMillis {
            val job = launch { // 부모
                launch { // 자식 1
                    println("launch1: ${Thread.currentThread().name}")
                    delay(5000L)
                }

                launch { // 자식 2
                    println("launch2: ${Thread.currentThread().name}")
                    delay(10L)
                }
            }
            job.join()
        }
        println(elapsed)
    }
})

suspend fun printContext(id: String) {
    val str = with(StringBuilder()) {
        append("$id / ${Thread.currentThread().name}")
        appendLine()
        append(currentCoroutineContext())
        appendLine()
        toString()
    }
    println(str)
}

suspend fun printContextWithDelay(id: String, time: Long, block: () -> Unit) {
    println(coroutineContext[Job])
    println("$id: ${Thread.currentThread().name}")
    delay(time)
    block()
}