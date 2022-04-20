package me.wonsik.order.demo.order.coroutines.coroutine

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.random.Random


/**
 * @author 정원식 (wonsik.cheung)
 *
 * kotlin coroutine test
 * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/README.md
 */
@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class CoroutineRunTest {

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/README.md#controlling-the-virtual-time
     */
    @Test
    @DisplayName("Controlling virtual time")
    fun testFoo() = runTest {
        println(currentCoroutineContext())

        launch {
            println(1)   // executes during runCurrent()
            delay(1_000) // suspends until time is advanced by at least 1_000
            println(2)   // executes during advanceTimeBy(2_000)
            delay(500)   // suspends until the time is advanced by another 500 ms
            println(3)   // also executes during advanceTimeBy(2_000)
            delay(5_000) // will suspend by another 4_500 ms
            println(4)   // executes during advanceUntilIdle()
        }
        // the child coroutine has not run yet
        runCurrent()
        // the child coroutine has called println(1), and is suspended on delay(1_000)
        advanceTimeBy(2_000) // progress time, this will cause two calls to `delay` to resume
        // the child coroutine has called println(2) and println(3) and suspends for another 4_500 virtual milliseconds
        advanceUntilIdle() // will run the child coroutine to completion
        currentTime shouldBe 6500 // the child coroutine finished at virtual time of 6_500 milliseconds
    }

    @Test
    @DisplayName("Delay in Different Coroutines")
    fun testBar() = runTest {
        val job = launch {
            println(coroutineContext[CoroutineDispatcher])
            val value = random()
            println(value)
        }

        println("Hello")
        println(coroutineContext[CoroutineDispatcher])
        println(Thread.currentThread().name)
        delay(1000L)
        println("World")

        runCurrent()
        advanceTimeBy(750L)
        println("Current Times: $currentTime")  // 1750
        advanceTimeBy(250L)
        println("Current Times: $currentTime")  // 2000
    }

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/README.md#using-multiple-test-dispatchers
     */
    @Test
    @DisplayName("실행 순서는 가장 delay 가 적은 순서대로 진행됨")
    fun testWithMultipleDelays() = runTest {
        launch {
            delay(1_000)
            println("1. $currentTime") // 1000
            delay(200)
            println("2. $currentTime") // 1200
            delay(2_000)
            println("4. $currentTime") // 3200
        }
        val deferred = async {
            println("===== Start =====")
            delay(3_000)
            println("3. $currentTime") // 3000
            delay(500)
            println("5. $currentTime") // 3500
            println("===== End =====")
        }
        deferred.await()
    }
}

@ExperimentalStdlibApi
suspend fun random(): Int {
    println(coroutineContext[CoroutineDispatcher])
    println(Thread.currentThread().name)
    delay(500)
    
    return Random.nextInt(0, 500)
}
