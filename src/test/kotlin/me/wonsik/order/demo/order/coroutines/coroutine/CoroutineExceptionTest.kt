package me.wonsik.order.demo.order.coroutines.coroutine


import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/6">CEH 와 슈퍼 바이저 잡</a>
 *
 * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/test/CoroutineExceptionHandlerTest.kt
 */
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class CoroutineExTest {

    @Test
    @DisplayName("without Coroutine Exception Handler")
    fun testFoo() = runTest {

        val atomicInteger: AtomicInteger = AtomicInteger(0)

        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch {
            println("scope: ${Thread.currentThread().name}")

            val cj1 = launch { plusOneWithDelay(atomicInteger, 500) }
            val cj2 = launch { printRandom() }
            val cj3 = launch { throwException() }
            val cj4 = launch { plusOneWithDelay(atomicInteger, 1500) }

            joinAll(cj1, cj2, cj3, cj4)
        }

        job.join()

        atomicInteger.get() shouldBe 1

        // Exception Occurred
        // Exception in thread "DefaultDispatcher-worker-3 @coroutine#11" java.lang.IllegalStateException
        //	at me.wonsik.order.demo.order.coroutines.coroutine.CoroutineExceptionTestKt.throwException(CoroutineExceptionTest.kt:161)
        //	at me.wonsik.order.demo.order.coroutines.coroutine.CoroutineExceptionTestKt$throwException$1.invokeSuspend(CoroutineExceptionTest.kt)
        //	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        //	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
        //	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
        //	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750)
        //	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
        //	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)
    }

    @Test
    @DisplayName("Coroutine Exception Handler")
    fun testBar() = runTest {

        val atomicInteger = AtomicInteger(0)

        val handler = CoroutineExceptionHandler { _, ex ->
            println("Something Happened: $ex")
        }

        val scope = CoroutineScope(Dispatchers.Default + handler)
        val cj1 = scope.launch { plusOneWithDelay(atomicInteger, 500) }
        val cj2 = scope.launch { printRandom() }
        val cj3 = scope.launch { throwException() }
        val cj4 = scope.launch { plusOneWithDelay(atomicInteger, 1500) }

        joinAll(cj1, cj2, cj3, cj4)

        atomicInteger.get() shouldBe 1
    }

    @Test
    @DisplayName("Coroutine Exception Handler + Supervisor Job")
    fun testSupervisorJob() = runTest {

        val atomicInteger = AtomicInteger(0)

        val handler = CoroutineExceptionHandler { _, ex ->
            println("Something Happened: $ex")
        }

        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob() + handler)
        val cj1 = scope.launch { plusOneWithDelay(atomicInteger, 500) }
        val cj2 = scope.launch { printRandom() }
        val cj3 = scope.launch { throwException() }
        val cj4 = scope.launch { plusOneWithDelay(atomicInteger, 1500) }

        joinAll(cj1, cj2, cj3, cj4)

        atomicInteger.get() shouldBe 2
    }

    @Test
    @DisplayName("Supervisor Scope = Coroutine Scope + Supervisor Job")
    fun testSupervisorScope() = runTest {

        val atomicInteger = AtomicInteger(0)

        val scope = CoroutineScope(Dispatchers.IO)

        val handler = CoroutineExceptionHandler { _, ex ->
            println("Something Happened: $ex")
        }

        val job = scope.launch {
            supervisorScope {
                val cj1 = launch { plusOneWithDelay(atomicInteger, 500) }
                val cj2 = launch { printRandom() }
                val cj3 = launch(handler) { throwException() }   // 자식의 실패가 부모에게 전달되지 않음
                // 따라서 예외 핸들링 필요
                val cj4 = launch { plusOneWithDelay(atomicInteger, 1500) }

                joinAll(cj1, cj2, cj3, cj4)
            }
        }
        job.join()

        atomicInteger.get() shouldBe 2
    }
}

suspend fun printRandom() {
    delay(1000L)
    println("printRandom: ${Thread.currentThread().name}")
    println(Random.nextInt(0, 500))
}

suspend fun throwException() {
    delay(500L)
    println("throwException: ${Thread.currentThread().name}")
    throw IllegalStateException()
}

suspend fun plusOneWithDelay(atomicInteger: AtomicInteger, delay: Long) {
    delay(delay)
    atomicInteger.addAndGet(1)
}
