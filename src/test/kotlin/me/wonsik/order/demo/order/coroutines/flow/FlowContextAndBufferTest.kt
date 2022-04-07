package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/10">플로우 컨텍스트</a>
 * @see <a href="https://dalinaum.github.io/coroutines-example/11">플로우 버퍼링</a>
 */
internal class FlowContextAndBufferTest(): FreeSpec({
    "Flow 는 현재 컨텍스트에서 호출됨" {
        runBlocking {
            launch(Dispatchers.IO) {
                simple().collect { value -> log("$value 를 받음.") }
            }
        }
    }

    "flowOn 연산자" {
        runBlocking {
            launch(Dispatchers.IO) {
                simple().flowOn(Dispatchers.Default)
                    .collect { value -> log("$value 를 받음.") }
            }
        }
    }

    "buffer" {
        runBlocking {
            val time = measureTimeMillis {
                simpleWithDelay().buffer()
                    .collect { value ->
                        delay(300)
                        println(value)
                    }
            }
            println("Collected in $time ms")    // 100 + 3 * 300 = 1000
        }
    }

    "conflate" {
        runBlocking {
            val time = measureTimeMillis {
                simpleWithDelay().conflate()
                    .collect { value ->
                        delay(300)
                        println(value)
                    }
            }
            println("Collected in $time ms")    // 100 + 2 * 300 = 700
        }
    }

    "collectLatest" {
        runBlocking {
            val time = measureTimeMillis {
                simpleWithDelay().conflate()
                    .collect { value ->
                        println("값 $value 를 처리하기 시작합니다.")
                        delay(300)
                        println(value)
                        println("값 $value 처리가 완료되었습니다.")
                    }
            }
            println("Collected in $time ms")    // 100 + 2 * 300 = 700
        }
    }
})

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")
fun simple(): Flow<Int> = flow {
    for (i in 1..10) {
        log("값 ${i}를 emit합니다.")
        emit(i)
    }
}
fun simpleWithDelay(): Flow<Int> = flow {
    repeat(3) {
        delay(100)
        emit(it)
    }
}
