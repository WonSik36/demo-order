package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


/**
 * @author 정원식 (wonsik.cheung)
 */
class FlowExceptionTest : FreeSpec({
    "try-catch 1" {
        try {
            oneTwoThree().collect { value ->
                println(value)
                check(value <= 1) { "Collected $value" }
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    "try-catch 2" {
        try {
            oneTwoThreeWithCheck().collect { value ->
                println(value)
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    "catch" {
        oneTwoThreeWithCheck()
            .catch { e -> emit("Caught in catch: $e") }
            .collect { value ->
                println(value)
            }
    }

    "catch affects only upstream" {
        try {
            oneTwoThreeWithCheck()
                .catch { e -> emit("Caught in catch: $e") }
                .collect { value ->
                    throw RuntimeException("RuntimeException: $value")
                }
        } catch (e: Throwable) {
            println("Caught in try-catch: $e")
        }
    }
})

fun oneTwoThree() = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i) // emit next value
    }
}

fun oneTwoThreeWithCheck() = oneTwoThree()
    .map { value ->
        check(value <= 1) { "Crashed on $value" }
        "string $value"
    }