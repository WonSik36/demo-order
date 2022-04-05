package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/10">플로우 컨텍스트</a>
 */
internal class FlowContextTest(): FreeSpec({
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
})

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")
fun simple(): Flow<Int> = flow {
    for (i in 1..10) {
        log("값 ${i}를 emit합니다.")
        emit(i)
    }
}