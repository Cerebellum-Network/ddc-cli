package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.ConsumerConfig
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Piece
import network.cere.ddc.client.producer.ProducerConfig
import picocli.CommandLine
import java.time.Instant
import java.util.*

@CommandLine.Command(name = "produce")
class ProduceCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

    @CommandLine.Option(
        names = ["-i", "--id"],
        description = ["Id of the piece (default - generate random UUID"]
    )
    var id: String? = null

    @CommandLine.Option(
        names = ["-u", "--user", "--userPubKey"],
        defaultValue = "cli-user",
        description = ["User public key to specify in data piece"]
    )
    lateinit var userPubKey: String

    @CommandLine.Option(
        names = ["-t", "--timestamp"],
        description = ["Timestamp of the piece in iso 8601 format (default - now)"]
    )
    var timestamp: Instant? = null

    @CommandLine.Option(names = ["-d", "--data"], required = true, description = ["Data to be stored in DDC"])
    lateinit var data: String

    @CommandLine.Option(
        names = ["--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    override fun run() {
        val producerConfig = readProducerConfig()
        val ddcProducer = DdcProducer(producerConfig)

        val res = ddcProducer.send(
            Piece(
                id = id ?: UUID.randomUUID().toString(),
                appPubKey = producerConfig.appPubKey,
                userPubKey = userPubKey,
                timestamp = timestamp ?: Instant.now(),
                data = data
            )
        )
            .await().indefinitely()

        println("cid: ${res.cid}")
    }

    private fun readProducerConfig(): ProducerConfig {
        val configOptions = ddcCliConfigFile.read(profile)

        val appPubKey = configOptions[DdcCliConfigFile.APP_PUB_KEY_CONFIG]
        if (appPubKey == null || appPubKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPubKey. Please use 'configure' command.")
        }

        val appPrivKey = configOptions[DdcCliConfigFile.APP_PRIV_KEY_CONFIG]
        if (appPrivKey == null || appPrivKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPrivKey. Please use 'configure' command.")
        }

        val bootstrapNodesAsString = configOptions[DdcCliConfigFile.BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        return ProducerConfig(appPubKey, appPrivKey, bootstrapNodesAsString.split(","))
    }
}
