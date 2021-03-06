package me.wonsik.order.demo.order.coroutines.flow

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/15">플로우 완료처리하기</a>
 * @see <a href="https://dalinaum.github.io/coroutines-example/16">플로우 런칭</a>
 */
class FlowCompletion : FreeSpec({
    "finally" - {
        "with exception" {
            try {
                exFlow().collect { println(it) }
            } finally {
                println("finally - with exception done")
            }
        }

        "without exception" {
            try {
                simpleFlow().collect { println(it) }
            } finally {
                println("finally - without exception done")
            }
        }
    }

    "onCompletion" - {
        "with exception" {
            exFlow()
                .onCompletion { println("onCompletion - with exception done") }
                .collect { println(it) }
        }

        "without exception" {
            simpleFlow()
                .onCompletion { println("onCompletion - with exception done") }
                .collect { println(it) }
        }

        "cause" {
            exFlow()
                .onCompletion { cause ->
                    if (cause == null) {
                        println("exception was not occurred")
                    } else {
                        println("exception was occurred")
                    }
                }
                .collect { println(it) }
        }
    }

    "launchIn" - {
        "without launchIn" {
            simpleFlow()
                .onEach { delay(100) }
                .onEach { event -> println("${currentCoroutineContext().job} Event: $event") }
                .collect()
            // blocking
            println("${currentCoroutineContext().job} Done")
        }

        "with launchIn" {
            simpleFlow()
                .onEach { delay(100) }
                .onEach { event -> println("${currentCoroutineContext().job} Event: $event") }
                .launchIn(this) // scope.launch { collect() }

            println("${currentCoroutineContext().job} Done")
        }
    }
})

fun simpleFlow() = flow {
    for (i in 1..3) {
        emit(i)
    }
}

fun exFlow() = flow {
    for (i in 1..3) {
        emit(i)
    }
    throw IllegalStateException("exFlow: throw ex")
}