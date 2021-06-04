package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PUB_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.PARTITION_POLL_INTERVAL_MS_CONFIG
import network.cere.ddc.client.consumer.ConsumerConfig
import network.cere.ddc.client.consumer.DataQuery
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine
import java.time.Instant

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
        names = ["--decrypt"],
        description = ["Decrypt data"]
    )
    var decrypt: Boolean = false

    @CommandLine.Option(
        names = ["--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val consumerConfig = readConsumerConfig(configOptions)

        val ddcConsumer = DdcConsumer(
            consumerConfig,
            Vertx.vertx(
                VertxOptions().setFileSystemOptions(
                    FileSystemOptions().setClassPathResolvingEnabled(false)
                )
            ),
        )

        val dataQuery = if (from != null && to != null) {
            DataQuery(from.toString(), to.toString(), fields)
        } else {
            DataQuery("", "", listOf())
        }

        if (decrypt) {
            consumeDecrypted(configOptions, ddcConsumer, dataQuery)
        } else {
            consumeEncrypted(ddcConsumer, dataQuery)
        }

        Thread.sleep(CONSUMING_SESSION_IN_MS)
    }

    private fun consumeDecrypted(
        configOptions: Map<String, String>,
        ddcConsumer: DdcConsumer,
        dataQuery: DataQuery
    ) {
        val encryptionConfig = ddcCliConfigFile.readEncryptionConfig(configOptions)
        val appMasterEncryptionKey = CryptoSecretKey(encryptionConfig.masterEncryptionKey)

        ddcConsumer.consume(streamId, dataQuery).subscribe().with(
            { cr ->
                try {
                    cr.piece.data =
                        appMasterEncryptionKey.decryptWithScopes(cr.piece.data!!, encryptionConfig.encryptionJsonPaths)
                    println(cr.piece)
                } catch (e: Exception) {
                    println("Can't decrypt data: " + cr.piece.data)
                }
            },
            { e -> println(e) }
        )
    }

    private fun consumeEncrypted(
        ddcConsumer: DdcConsumer,
        dataQuery: DataQuery
    ) {
        ddcConsumer.consume(streamId, dataQuery)
            .subscribe().with({ cr -> println(cr.piece) }, { e -> println(e) })
    }

    private fun readConsumerConfig(configOptions: Map<String, String>): ConsumerConfig {
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
