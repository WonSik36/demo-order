package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/8">플로우 기초</a>
 */
class FlowBasicTest: FreeSpec({
    "Hello Flow" {
        runBlocking {
            flowSomething().collect { value ->
                println(value)
            }
        }
    }

    "Cancel Flow" {
        runBlocking<Unit> {
            val result = withTimeoutOrNull(500L) {
                flowSomething().collect { value ->
                    println(value)
                }
                true
            } ?: false
            if (!result) {
                println("취소되었습니다.")
            }
        }
    }

    "flowOf" {
        runBlocking {
            flowOf(1,2,3,4,5).collect { println(it) }
        }
    }

    "asFlow" {
        runBlocking {
            listOf(1,2,3,4,5).asFlow().collect { println(it) }

            (6..10).asFlow().collect{ println(it) }
        }
    }

    "with coroutines" {
        runBlocking {
            coroutineString()

            flowString().collect { println(it) }
        }
    }
})

fun flowSomething(): Flow<Int> = flow {
    println(this)
    repeat(10) {
        emit(Random.nextInt(0, 500))
        delay(100L)
    }
}

fun flowString(): Flow<String> = flow {
    repeat(10) {
        emit("${Thread.currentThread().name}: $it")
        emit(this.toString())
        delay(100L)
    }
}

fun CoroutineScope.coroutineString() = launch {
    repeat(10) {
        println("${Thread.currentThread().name}: $it")
        println(this)
        delay(100L)
    }
}