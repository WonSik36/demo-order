package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/7">공유 객체, Mutex, Actor</a>
 */
class CoroutineConcurrencyTest : FreeSpec({
    "Thread-safe in coroutines" - {
        "problem case" {
            var counter = 0

            runBlocking {
                withContext(Dispatchers.Default) {
                    massiveRun {
                        // println(Thread.currentThread().name)
                        counter++
                    }
                }
                println("Counter = $counter")
            }
        }

        "Single Thread - 1" {
            var counter = 0

            runBlocking {
                val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
                withContext(context) {
                    massiveRun {
                        // println(Thread.currentThread().name)
                        counter++
                    }
                }
                println("Counter = $counter")
            }
        }

        "Thread-safe data structure" {
            val counter = AtomicInteger(0)

            runBlocking {
                withContext(Dispatchers.Default) {
                    massiveRun {
                        // println(Thread.currentThread().name)
                        counter.incrementAndGet()
                    }
                }
                println("Counter = $counter")
            }
        }

        "Single thread - 2" {
            var counter = 0
            val counterContext = newSingleThreadContext("CounterContext")

            runBlocking {
                withContext(counterContext) {
                    massiveRun {
                        // println(Thread.currentThread().name)
                        counter++
                    }
                }
                println("Counter = $counter")
            }
        }

        "Mutex (Mutual Exclusion)" {
            var counter = 0
            val mutex = Mutex()

            runBlocking {
                withContext(Dispatchers.Default) {
                    massiveRun {
                        mutex.withLock {
                            counter++
                        }
                    }
                }
                println("Counter = $counter")
            }
        }

        "Actor" {
            runBlocking<Unit> {
                val counter = createCounterActor(0)
                withContext(Dispatchers.Default) {
                    massiveRun {
                        counter.send(IncCounter)    // suspension point
                    }
                }

                val response = CompletableDeferred<Int>()
                counter.send(GetCounter(response))  // suspension point
                println("Counter = ${response.await()}")    // suspension point
                counter.close()
            }
        }
    }
})


suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 10000 // 시작할 코루틴의 갯수
    val k = 10 // 코루틴 내에서 반복할 횟수
    val elapsed = measureTimeMillis {
        coroutineScope { // scope for coroutines
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("$elapsed ms동안 ${n * k}개의 액션을 수행했습니다.")
}

// Actor
sealed class CounterMsg
object IncCounter : CounterMsg()
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()

fun CoroutineScope.createCounterActor(initCount: Int = 0) = actor<CounterMsg> {
    var count = initCount

    for (msg in channel) {  // suspension point
        when (msg) {
            is IncCounter -> count++
            is GetCounter -> msg.response.complete(count)
        }
    }
}
