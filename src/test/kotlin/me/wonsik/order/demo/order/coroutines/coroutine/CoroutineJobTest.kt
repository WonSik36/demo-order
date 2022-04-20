package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.RuntimeException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/3">취소와 타임아웃</a>
 */
class CoroutineJobTest : FreeSpec({

    "Cancel Coroutine Job" {
        coroutineScope {
            val job1 = launch {
                println("launch1: ${Thread.currentThread().name}")
                delay(1000L)
                println("3!")
            }

            val job2 = launch {
                println("launch2: ${Thread.currentThread().name}")
                println("1!")
            }

            val job3 = launch {
                println("launch3: ${Thread.currentThread().name}")
                delay(500L)
                println("2!")
            }

            delay(800L)
            job1.cancel()
            job2.cancel()
            job3.cancel()
            println("4!")
        }

        println("testScope: ${Thread.currentThread().name}")
        println("5!")
    }

    "Irrevocable Job" {
        val job = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while(i <= 10) {
                val currentTime = System.currentTimeMillis()
                if(currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job.cancel()
        println("Count Done")

//        1
//        2
//        Count Done
//        3
//        4
//        5
//        6
//        7
//        8
//        9
//        10
    }

    "Cancel And Join" {
        val job = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while(i <= 10) {
                val currentTime = System.currentTimeMillis()
                if(currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job.cancelAndJoin()
        println("Count Done")

//        1
//        2
//        3
//        4
//        5
//        6
//        7
//        8
//        9
//        10
//        Count Done
    }

    "Cancellable Job & with Resource" {
        val job = launch(Dispatchers.Default) {
            try {
                delay(500L)
                println("Job is not finished")
            } catch (ex: RuntimeException){
                println(ex) // JobCancellationException
            } finally {
                println("Job is finished")
            }
        }

        delay(200L)
        job.cancelAndJoin()

        // Job is finished
    }

    "Non Cancellable Job" {
        val job = launch(Dispatchers.Default) {
            withContext(NonCancellable) {
                var i = 1
                var nextTime = System.currentTimeMillis() + 100L

                while(i <= 10 && isActive) {
                    val currentTime = System.currentTimeMillis()
                    if(currentTime >= nextTime) {
                        println(i)
                        nextTime = currentTime + 100L
                        i++
                    }
                }
            }
            delay(100L)
            println("Cancellable")
        }

        delay(200L)
        job.cancelAndJoin()
        println("Count Done")

//        1
//        2
//        3
//        4
//        5
//        6
//        7
//        8
//        9
//        10
//        Count Done
    }

    "withTimeout" {
        withTimeout(500) {
            val job = launch(Dispatchers.Default) {
                var i = 1
                var nextTime = System.currentTimeMillis() + 100L

                while(i <= 10 && isActive) {
                    val currentTime = System.currentTimeMillis()
                    if(currentTime >= nextTime) {
                        println(i)
                        nextTime = currentTime + 100L
                        i++
                    }
                }
            }

            job.join()
        }

        // TimeoutCancellationException
    }

    "withTimeoutOrNull" {
        val result = withTimeoutOrNull(500) {
            val job = launch(Dispatchers.Default) {
                var i = 1
                var nextTime = System.currentTimeMillis() + 100L

                while(i <= 10 && isActive) {
                    val currentTime = System.currentTimeMillis()
                    if(currentTime >= nextTime) {
                        println(i)
                        nextTime = currentTime + 100L
                        i++
                    }
                }
            }

            job.join()
        } ?: false

        println(result)
    }
})
