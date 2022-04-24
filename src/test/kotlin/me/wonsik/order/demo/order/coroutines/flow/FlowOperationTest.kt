package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random
import kotlin.random.nextInt


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/9">플로우 연산</a>
 * @see <a href="https://dalinaum.github.io/coroutines-example/12">플로우 결합하기</a>
 * @see <a href="https://dalinaum.github.io/coroutines-example/13">플로우 플래트닝하기</a>
 */
class FlowOperationTest: FreeSpec({
    "map" {
        flowRandomInt().map {
            "$it $it"
        }.collect { value ->
            println(value)
        }
    }

    "filter" {
        (1 .. 20).asFlow()
            .filter { it % 2 == 0 }
            .collect { value ->
                println(value)
            }
    }

    "filterNot" {
        (1 .. 20).asFlow()
            .filterNot { it % 2 == 0 }
            .collect { value ->
                println(value)
            }
    }

    "transform" {
        (1 .. 20).asFlow()
            .transform {
                if (it % 2 != 0) {
                    emit(it * 2)
                    emit(it * 3)
                }
            }
            .collect { value ->
                println(value)
            }
    }

    "take" {
        (1 .. 20).asFlow()
            .take(5)
            .collect {
                println(it)
            }
    }

    "takeWhile" {
        (1 .. 20).asFlow()
            .takeWhile {
                it < 15
            }
            .collect {
                println(it)
            }
    }

    "drop" {
        (1 .. 20).asFlow()
            .drop(5)
            .collect {
                println(it)
            }
    }

    "dropWhile" {
        ((1 .. 20) + (1 .. 20)).asFlow()
            .dropWhile {
                it < 15
            }
            .collect {
                println(it)
            }
    }

    "reduce" {
        val value = (1..10).asFlow()
            .reduce{ a,b ->
                a + b
            }

        println(value)
    }

    "fold" {
        val value = (1..10).asFlow()
            .fold(10) { a,b ->
                a + b
            }

        println(value)
    }

    "count" {
        val value1 = (1..10).asFlow()
            .count()

        println(value1)

        val value2 = (1..10).asFlow()
            .count {
                it % 2 == 0
            }

        println(value2)
    }

    "toList, toSet" {
        val list = (1..10).asFlow()
            .filter { it % 2 == 0 }
            .toList()

        println(list)

        val set = (1..10).asFlow()
            .filter { it % 2 == 1 }
            .toSet()

        println(set)
    }

    "zip" {
        val nums = (1..3).asFlow()
        val strs = flowOf("one", "two", "three")

        nums.zip(strs) { a,b ->
            "$a is $b"
        }.collect { println(it) }

        // 가장 짧은 길이로 zip 됨
        val nums2 = (1..2).asFlow()
        nums2.zip(strs) { a,b ->
            "$a is $b"
        }.collect { println(it) }

        val strs2 = flowOf("one", "two")
        nums.zip(strs2) { a,b ->
            "$a is $b"
        }.collect { println(it) }
    }

    "combine" {
        val nums = (1..3).asFlow().onEach { delay(100L) }
        val strs = flowOf("one", "two", "three").onEach { delay(200L) }

        // 가장 최신의 데이터가 들어오면 emit
        nums.combine(strs) { a,b ->
            "$a is $b"
        }.collect { println(it) }

        // 1 is one
        // 2 is one
        // 3 is one
        // 3 is two
        // 3 is three
    }
})

fun flowRandomInt() = flow {
    repeat(10) {
        emit(Random.nextInt(0, 500))
        delay(10L)
    }
}
