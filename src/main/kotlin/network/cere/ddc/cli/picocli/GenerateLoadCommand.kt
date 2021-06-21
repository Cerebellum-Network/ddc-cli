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
        defaultValue = "1",
        description = ["Number of concurrent users (default - 1)"]
    )
    var users: Int? = 0

    @CommandLine.Option(
        names = ["-n", "--number"],
        defaultValue = Int.MAX_VALUE.toString(),
        description = ["Number of pieces per user (default - int max value)"]
    )
    var number: Int? = 0

    @CommandLine.Option(
        names = ["-i", "--interval"],
        defaultValue = "1s",
        description = ["Interval between user requests (default - 1s)"]
    )
    var interval: Duration? = null

    @CommandLine.Option(
        names = ["-s", "--size"],
        defaultValue = "1000",
        description = ["Piece size to be generted in bytes (default - 1000)"]
    )
    var size: Int? = 0

    @CommandLine.Option(
        names = ["--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

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
            repeat(users!!) {
                val userPubKey = UUID.randomUUID().toString()
                launch {
                    repeat(number!!) {
                        val data = "0".repeat(size!!)

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

                        delay(interval!!.toMillis())
                    }
                }
            }
        }
    }
}
