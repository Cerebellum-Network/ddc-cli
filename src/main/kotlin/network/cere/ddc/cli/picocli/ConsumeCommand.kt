package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PUB_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.PARTITION_POLL_INTERVAL_MS_CONFIG
import network.cere.ddc.client.consumer.ConsumerConfig
import network.cere.ddc.client.consumer.DataQuery
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Piece
import network.cere.ddc.client.producer.ProducerConfig
import picocli.CommandLine
import java.time.Instant
import java.util.*

@CommandLine.Command(name = "consume")
class ConsumeCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

    companion object {
        private const val CONSUMING_SESSION_IN_MS = 3_600_000L
    }

    @CommandLine.Option(
        names = ["--stream-id"],
        defaultValue = "cli-stream",
        description = ["The id of stream to consume"]
    )
    lateinit var streamId: String

    @CommandLine.Option(
        names = ["--from"],
        description = ["Date from in iso 8601 format"]
    )
    var from: Instant? = null

    @CommandLine.Option(
        names = ["--to"],
        description = ["Date to in iso 8601 format"]
    )
    var to: Instant? = null

    @CommandLine.Option(
        names = ["--fields"],
        description = ["List of fields to query from JSON (dot notation). Example: age,name.last"]
    )
    var fields: List<String> = listOf()

    @CommandLine.Option(
        names = ["--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    override fun run() {
        val consumerConfig = readConsumerConfig()
        val ddcConsumer = DdcConsumer(consumerConfig)

        val dataQuery = if (from != null && to != null) {
            DataQuery(from.toString(), to.toString(), fields)
        } else {
            DataQuery("", "", listOf())
        }

        ddcConsumer.consume(streamId, dataQuery)
            .subscribe().with({ cr -> println(cr.piece) }, { e -> println(e) })

        Thread.sleep(CONSUMING_SESSION_IN_MS)
    }

    private fun readConsumerConfig(): ConsumerConfig {
        val configOptions = ddcCliConfigFile.read(profile)

        val appPubKey = configOptions[APP_PUB_KEY_CONFIG]
        if (appPubKey == null || appPubKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPubKey. Please use 'configure' command.")
        }

        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        val partitionPollIntervalMsAsString = configOptions[PARTITION_POLL_INTERVAL_MS_CONFIG]
        if (partitionPollIntervalMsAsString != null && partitionPollIntervalMsAsString.isNotEmpty()) {
            val partitionPollIntervalMs = partitionPollIntervalMsAsString.toInt()
            return ConsumerConfig(appPubKey, bootstrapNodesAsString.split(","), partitionPollIntervalMs)
        }

        return ConsumerConfig(appPubKey, bootstrapNodesAsString.split(","))
    }
}
