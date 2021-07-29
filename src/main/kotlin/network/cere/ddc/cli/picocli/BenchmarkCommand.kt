package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Piece
import picocli.CommandLine
import java.time.Instant
import java.util.*
import kotlinx.coroutines.*
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random

@CommandLine.Command(name = "benchmark")
class BenchmarkCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

    companion object {
        const val APPROXIMATE_PIECE_METADATA_OVERHEAD = 500 // metadata + indexes

        const val BYTES_PER_WCU = 4 * 1 shl 10 // 4 KB
        const val BYTES_PER_RCU = 4 * 1 shl 10 // 4 KB
    }

    @CommandLine.Option(
        names = ["-u", "--users"],
        description = ["Number of concurrent users (default - 100)"]
    )
    var users: Int = 100

    @CommandLine.Option(
        names = ["-d", "--duration"],
        description = ["Benchmark data generation duration in milliseconds (default - 1 minute)"]
    )
    var durationInMs: Long = 60 * 1000

    @CommandLine.Option(
        names = ["-sMin", "--sizeMin"],
        description = ["Min piece size to be generated in bytes (default - 4KB)"]
    )
    var sizeMin: Int = 4 * 1 shl 10 // 4 KB

    @CommandLine.Option(
        names = ["-sMax", "--sizeMax"],
        description = ["Max piece size to be generated in bytes (default - 4MB)"]
    )
    var sizeMax: Int = 4 * 1 shl 2 * 10 // 4 MB

    @CommandLine.Option(
        names = ["-p", "--profile"],
        description = ["Configuration profile to use)"]
    )
    var profile: String = DdcCliConfigFile.DEFAULT_PROFILE

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val consumerConfig = ddcCliConfigFile.readConsumerConfig(configOptions)
        val vertx = Vertx.vertx(
            VertxOptions().setFileSystemOptions(
                FileSystemOptions().setClassPathResolvingEnabled(false)
            )
        )
        val ddcProducer = DdcProducer(producerConfig, vertx)
        val ddcConsumer: Consumer = DdcConsumer(consumerConfig, vertx)

        val benchmarkIsRunning = AtomicBoolean(true)
        val totalWcu = AtomicInteger(0)
        val totalRcu = AtomicInteger(0)

        // produce data
        val generationThreads = mutableListOf<Thread>()
        repeat(users) {
            val userPubKey = UUID.randomUUID().toString()
            val thread: Thread = thread {
                while (benchmarkIsRunning.get()) {
                    val pieceSize = Random.nextInt(sizeMin, sizeMax)
                    val dataSize = pieceSize - APPROXIMATE_PIECE_METADATA_OVERHEAD
                    val data = "0".repeat(dataSize)

                    ddcProducer.send(
                        Piece(
                            id = UUID.randomUUID().toString(),
                            appPubKey = producerConfig.appPubKey,
                            userPubKey = userPubKey,
                            timestamp = Instant.now(),
                            data = data
                        )
                    )
                        .await().indefinitely()

                    var wcu = pieceSize / BYTES_PER_WCU
                    if (pieceSize % BYTES_PER_WCU > 0) {
                        wcu++
                    }
                    totalWcu.addAndGet(wcu)
                }
            }
            generationThreads.add(thread)
        }

        sleep(durationInMs)
        benchmarkIsRunning.set(false)

        // consume data
        val consumingStart = System.currentTimeMillis()
        val bytesTransferred = AtomicInteger(0)

        ddcConsumer.getAppPieces().subscribe().asStream().forEach { p ->
            bytesTransferred.addAndGet(getSize(p))
        }

        var rcu = bytesTransferred.get() / BYTES_PER_RCU
        if (bytesTransferred.get() % BYTES_PER_RCU > 0) {
            rcu++
        }
        totalRcu.addAndGet(rcu)

        val consumingDurationIsMs = System.currentTimeMillis() - consumingStart

        // print result
        println("Total WCU: ${totalWcu.get()}")
        println("Total RCU: ${totalRcu.get()}")
        println("WCU/sec: ${totalWcu.get() / (durationInMs / 1000)}")
        println("RCU/sec: ${totalRcu.get() / (consumingDurationIsMs / 1000)}")
    }

    private fun getSize(piece: network.cere.ddc.client.consumer.Piece): Int {
        return piece.id!!.length +
                piece.appPubKey!!.length +
                piece.userPubKey!!.length +
                piece.timestamp!!.toString().length +
                piece.data!!.length
    }
}
