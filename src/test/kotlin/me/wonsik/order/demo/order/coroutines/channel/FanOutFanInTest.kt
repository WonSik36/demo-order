package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/19">팬 아웃, 팬 인</a>
 */
class FanOutFanInTest : FreeSpec({

    "Fan out" {
        val producer = produceNumbers()

        repeat(5) {
            launch {
                // Producer < Consumer
                producer.consumeEach { value ->
                    println("${it}가 ${value}을 받았습니다.")
                }
            }
        }

        delay(20L)
        producer.cancel()   // ProducerCoroutine -> ChannelCoroutine 에서 cancel 되면 안의 코루틴도 cancel 함
    }

    "Fan in" {
        val channel = Channel<Int>()

        // Producer > Consumer
        produceNumbers(channel, 1, 100)
        produceNumbers(channel, 2, 100)

        retrieveNumbers(channel)

        delay(1000L)
        coroutineContext.cancelChildren()
    }
})

fun CoroutineScope.produceNumbers(channel: SendChannel<Int>, from: Int, interval: Long) = launch {
    var x = from
    while(true) {
        channel.send(x)
        x += 2
        delay(interval)
    }
}

fun CoroutineScope.retrieveNumbers(channel: ReceiveChannel<Int>) = launch {
    channel.consumeEach {
        println("${it}을 받았습니다.")
    }
}