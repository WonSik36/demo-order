package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/17">채널 기초</a>
 */
class ChannelBasicTest : FreeSpec({
    "Basic" - {
        "send - receive" {
            val channel = Channel<Int>()
            launch {
                for (x in 1..10) {
                    println("${currentCoroutineContext().job} send: $x")
                    channel.send(x)     // suspension point
                }
            }

            println("시작")
            repeat(10) {
                println("${currentCoroutineContext().job} receive: ${channel.receive()}")  // suspension point
            }
            println("완료")
        }

        "trySend - tryReceive" {
            val channel = Channel<Int>(capacity = 5)

            println("시작")
            launch {
                for (x in 1..10) {
                    println("${currentCoroutineContext().job} trySend: $x")
                    channel.trySend(x)     // not suspension point
                }
            }.join()

            repeat(10) {
                println("${currentCoroutineContext().job} tryReceive: ${channel.tryReceive().getOrNull()}")  // not suspension point
            }
            println("완료")
        }
    }
})