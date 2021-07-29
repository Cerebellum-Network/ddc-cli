package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.producer.Piece
import picocli.CommandLine
import java.time.Duration
import java.time.Instant
import java.util.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@CommandLine.Command(name = "generate-load")
class GenerateLoadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-u", "--users"],
        description = ["Number of concurrent users (default - 1)"]
    )
    var users: Int = 1

    @CommandLine.Option(
        names = ["-n", "--number"],
        description = ["Number of pieces per user (default - int max value)"]
    )
    var number: Int = Int.MAX_VALUE

    @CommandLine.Option(
        names = ["-i", "--interval"],
        description = ["Interval between user requests (default - 1s)"]
    )
    var interval: Duration = Duration.ofSeconds(1)

    @CommandLine.Option(
        names = ["-s", "--size"],
        description = ["Piece size to be generated in bytes (default - 1000)"]
    )
    var size: Int = 1000

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val ddcProducer = buildProducer(producerConfig)

        val generationThreads = mutableListOf<Thread>()
        repeat(users) {
            val userPubKey = UUID.randomUUID().toString()
            val thread: Thread = thread {
                repeat(number) {
                    val data = "0".repeat(size)

                    val res = ddcProducer.send(
                        Piece(
                            id = UUID.randomUUID().toString(),
                            appPubKey = producerConfig.appPubKey,
                            userPubKey = userPubKey,
                            timestamp = Instant.now(),
                            data = data
                        )
                    )
                        .await().indefinitely()

                    println("cid: ${res.cid}")

                    sleep(interval.toMillis())
                }
            }
            generationThreads.add(thread)
        }
        generationThreads.forEach { it.join() }
    }
}
