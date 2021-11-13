package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.api.Metadata
import network.cere.ddc.client.producer.Piece
import picocli.CommandLine
import java.time.Instant
import java.util.*
import java.lang.Thread.sleep
import java.time.Duration
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
        description = ["Benchmark data generation duration in seconds (default - 1 min)"]
    )
    var durationInSec: Long = 60

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
        val producingStartTime = Instant.now().toString()
        produce(configOptions)
        val producingEndTime = Instant.now().toString()
        consume(configOptions, producingStartTime, producingEndTime)
    }

    private fun produce(configOptions: Map<String, String>) {
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val ddcProducer = buildProducer(producerConfig)

        val benchmarkIsRunning = AtomicBoolean(true)
        val totalRequests = AtomicLong(0)
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
                            data = data,
                            metadata = Metadata(
                                contentType = "image",
                                mimeType = "image/png",
                                customAttributes = mapOf(
                                    "minter" to userPubKey,
                                    "relId" to "123",
                                    "relType" to "nft",
                                    "title" to "title",
                                    "description" to "someDescription",
                                    "type" to "image",
                                    "contentType" to "image/png",
                                ),
                                isEncrypted = true,
                                encryptionAttributes = mapOf(
                                    "nonceHex" to "0x23491243dd781123cf2aba3123",
                                    "encryptionAlgorithm" to "ChaCha20",
                                    "encryptionTransformation" to "ChaCha20-Poly1305/None/NoPadding"
                                )
                            )
                        )
                    )
                        .await().indefinitely()

                    var wcu = pieceSize / BYTES_PER_WCU
                    if (pieceSize % BYTES_PER_WCU > 0) {
                        wcu++
                    }
                    totalRequests.incrementAndGet()
                    totalWcu.addAndGet(wcu.toLong())
                }
            }
            generationThreads.add(thread)
        }

        sleep(Duration.ofSeconds(durationInSec).toMillis())
        benchmarkIsRunning.set(false)

        // print result
        println("=".repeat(60))
        println("Producing")
        println("Total requests: ${totalRequests.get()}")
        println("Total WCU: ${totalWcu.get()}")
        println("Req/sec: ${totalRequests.get() / durationInSec}")
        println("WCU/sec: ${totalWcu.get() / durationInSec}")
    }

    private fun consume(configOptions: Map<String, String>, startTime: String, endTime: String) {
        val ddcConsumer = buildConsumer(configOptions)

        val totalBytes = AtomicLong(0)
        val totalRcu = AtomicLong(0)
        val consumingStart = System.currentTimeMillis()

        ddcConsumer.getAppPieces(startTime, endTime).subscribe().asStream().forEach { p ->
            totalBytes.addAndGet(getSize(p).toLong())
        }

        var rcu = totalBytes.get() / BYTES_PER_RCU
        if (totalBytes.get() % BYTES_PER_RCU > 0) {
            rcu++
        }
        totalRcu.addAndGet(rcu)

        val durationInSec = (System.currentTimeMillis() - consumingStart) / 1000

        // print result
        println("=".repeat(60))
        println("Consuming")
        println("Total bytes: ${totalBytes.get()}")
        println("Total RCU: ${totalRcu.get()}")
        println("RCU/sec: ${totalRcu.get() / durationInSec}")
    }

    private fun getSize(piece: network.cere.ddc.client.consumer.Piece): Int {
        return piece.id!!.length +
                piece.appPubKey!!.length +
                piece.userPubKey!!.length +
                piece.timestamp!!.toString().length +
                piece.data!!.length
    }
}
