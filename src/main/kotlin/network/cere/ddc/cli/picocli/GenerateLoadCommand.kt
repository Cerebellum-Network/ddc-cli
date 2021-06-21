package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Piece
import picocli.CommandLine
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlinx.coroutines.*

@CommandLine.Command(name = "generate-load")
class GenerateLoadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

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
    var interval: Duration = Duration.parse("1s")

    @CommandLine.Option(
        names = ["-s", "--size"],
        description = ["Piece size to be generted in bytes (default - 1000)"]
    )
    var size: Int = 1000

    @CommandLine.Option(
        names = ["--profile"],
        description = ["Configuration profile to use)"]
    )
    var profile: String = DdcCliConfigFile.DEFAULT_PROFILE

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val ddcProducer = DdcProducer(
            producerConfig,
            Vertx.vertx(
                VertxOptions().setFileSystemOptions(
                    FileSystemOptions().setClassPathResolvingEnabled(false)
                )
            ),
        )

        runBlocking {
            repeat(users) {
                val userPubKey = UUID.randomUUID().toString()
                launch {
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

                        delay(interval.toMillis())
                    }
                }
            }
        }
    }
}
