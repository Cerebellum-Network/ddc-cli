package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.client.consumer.OffsetReset
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine

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
        names = ["--offset-reset"],
        description = ["Configure the start of the data stream. Earliest - from the beginning. Latest - real time (old data isn't consumed). (default - earliest)"]
    )
    var offsetReset: OffsetReset = OffsetReset.EARLIEST

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
        val consumerConfig = ddcCliConfigFile.readConsumerConfig(configOptions)

        val ddcConsumer: Consumer = DdcConsumer(
            consumerConfig,
            Vertx.vertx(
                VertxOptions().setFileSystemOptions(
                    FileSystemOptions().setClassPathResolvingEnabled(false)
                )
            ),
        )

        if (decrypt) {
            consumeDecrypted(configOptions, ddcConsumer, fields, offsetReset)
        } else {
            consumeEncrypted(ddcConsumer, fields, offsetReset)
        }

        Thread.sleep(CONSUMING_SESSION_IN_MS)
    }

    private fun consumeDecrypted(
        configOptions: Map<String, String>,
        ddcConsumer: Consumer,
        fields: List<String>,
        offsetReset: OffsetReset
    ) {
        val encryptionConfig = ddcCliConfigFile.readEncryptionConfig(configOptions)
        val appMasterEncryptionKey = CryptoSecretKey(encryptionConfig.masterEncryptionKey)

        ddcConsumer.consume(streamId, fields, offsetReset).subscribe().with(
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
        ddcConsumer: Consumer,
        fields: List<String>,
        offsetReset: OffsetReset
    ) {
        ddcConsumer.consume(streamId, fields, offsetReset)
            .subscribe().with({ cr -> println(cr.piece) }, { e -> println(e) })
    }
}
