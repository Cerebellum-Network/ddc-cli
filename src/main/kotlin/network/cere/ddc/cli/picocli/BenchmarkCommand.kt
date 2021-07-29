package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.producer.Piece
import picocli.CommandLine
import java.time.Instant
import java.util.*
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.random.Random

@CommandLine.Command(name = "benchmark")
class BenchmarkCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

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
        description = ["Min piece size to be generated in bytes (default - 4 KB)"]
    )
    var sizeMin: Int = 4 * 1 shl 10 // 4 KB

    @CommandLine.Option(
        names = ["-sMax", "--sizeMax"],
        description = ["Max piece size to be generated in bytes (default - 400 KB)"]
    )
    var sizeMax: Int = 400 * 1 shl 10 // 400 KB

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        produce(configOptions)
        consume(configOptions)
    }

    private fun produce(configOptions: Map<String, String>) {
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val ddcProducer = buildProducer(producerConfig)

        val benchmarkIsRunning = AtomicBoolean(true)
        val totalWriteRequests = AtomicLong(0)
        val totalWcu = AtomicLong(0)

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
                    totalWriteRequests.incrementAndGet()
                    totalWcu.addAndGet(wcu.toLong())
                }
            }
            generationThreads.add(thread)
        }

        sleep(durationInMs)
        benchmarkIsRunning.set(false)

        // print result
        println("=".repeat(60))
        println("Producing")
        println("Total requests: ${totalWriteRequests.get()}")
        println("Total WCU: ${totalWcu.get()}")
        println("Req/sec: ${totalWriteRequests.get() / (durationInMs / 1000)}")
        println("WCU/sec: ${totalWcu.get() / (durationInMs / 1000)}")
    }

    private fun consume(configOptions: Map<String, String>) {
        val ddcConsumer = buildConsumer(configOptions)

        val totalBytesConsumed = AtomicLong(0)
        val totalRcu = AtomicLong(0)
        val consumingStart = System.currentTimeMillis()

        ddcConsumer.getAppPieces().subscribe().asStream().forEach { p ->
            totalBytesConsumed.addAndGet(getSize(p).toLong())
        }

        var rcu = totalBytesConsumed.get() / BYTES_PER_RCU
        if (totalBytesConsumed.get() % BYTES_PER_RCU > 0) {
            rcu++
        }
        totalRcu.addAndGet(rcu)

        val consumingDurationIsMs = System.currentTimeMillis() - consumingStart

        // print result
        println("=".repeat(60))
        println("Consuming")
        println("Total bytes: ${totalBytesConsumed.get()}")
        println("Total RCU: ${totalRcu.get()}")
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
