package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참고
 * @see <a href="https://dalinaum.github.io/coroutines-example/20">채널 버퍼링</a>
 */
class ChannelBufferTest : FreeSpec({
    "capacity" - {
        "buffer - 10" {
            val channel = channelWithBuffer(10)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "buffer - UNLIMITED" {
            val channel = channelWithBuffer(Channel.UNLIMITED, size = 100)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "buffer - RENDEZVOUS" {
            val channel = channelWithBuffer(Channel.RENDEZVOUS)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "buffer - CONFLATED" {
            val channel = channelWithBuffer(Channel.CONFLATED)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "buffer - BUFFERED" {
            val channel = channelWithBuffer(Channel.BUFFERED, size = 100)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }
    }

    "bufferOverflow" - {
        "SUSPEND" {
            val channel = channelWithBuffer(onBufferOverflow = BufferOverflow.SUSPEND)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "DROP_OLDEST" {
            val channel = channelWithBuffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }

        "DROP_LATEST" {
            val channel = channelWithBuffer(onBufferOverflow = BufferOverflow.DROP_LATEST)

            for (x in channel) {
                println("$x 수신")
                delay(10L)
            }
            println("완료")
        }
    }
})

fun CoroutineScope.channelWithBuffer(
    capacity: Int = 0,
    size: Int = 20,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    onUndeliveredElement: ((Int) -> Unit)? = null
): Channel<Int> {
    val channel = Channel<Int>(capacity, onBufferOverflow, onUndeliveredElement)
    launch {
        for (x in 1..size) {
            println("$x 전송중")
            channel.send(x)
        }
        channel.close()
    }

    return channel
}