package me.wonsik.order.demo.order.coroutines.coroutine

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.days


/**
 * @author 정원식 (wonsik.cheung)
 *
 * kotest 의 coroutine 환경이 제대로 설정되어 있는지 확인하는 regression 테스트
 */
@ExperimentalKotest
@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class CoroutineKotestRegressionTest : FunSpec()  {
    init {
        testCoroutineDispatcher = true

        test("a test with TestDispatcher should advance time virtually") {
            val currentTime1 = testCoroutineScheduler.currentTime
            currentTime1 shouldBe 0L
            testCoroutineScheduler.advanceTimeBy(1234)
            val currentTime2 = testCoroutineScheduler.currentTime
            currentTime2 shouldBe 1234
        }

        context("a context with a test dispatcher should be inherited by nested tests") {
            val dispatcher = coroutineContext[CoroutineDispatcher]
            test("nest me!") {
                coroutineContext[CoroutineDispatcher] shouldBe dispatcher
            }
        }

        test("delay controller should control time") {
            val duration = 1.days
            launch {
                delay(duration.inWholeMilliseconds)
            }
            // if this isn't working, the above test will just take forever
            testCoroutineScheduler.advanceTimeBy(duration.inWholeMilliseconds)
        }

        test("child coroutines should have same dispatcher") {
            println("parent Dispatcher")
            val parentDispatcher = coroutineContext[CoroutineDispatcher]
            println(parentDispatcher)
            launch(Job()) {
                println("child Dispatcher")
                val childDispatcher = coroutineContext[CoroutineDispatcher]
                println(childDispatcher)
            }


            testCoroutineScheduler.runCurrent()
        }
    }
}