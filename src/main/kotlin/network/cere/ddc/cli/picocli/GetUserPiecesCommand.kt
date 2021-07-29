package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine
import java.time.Instant

@CommandLine.Command(name = "get-user-pieces")
class GetUserPiecesCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    companion object {
        private const val CONSUMING_SESSION_IN_MS = 3_600_000L
    }

    @CommandLine.Option(
        names = ["-u", "--user", "--userPubKey"],
        required = true,
        description = ["User public key to specify in data piece"],
    )
    lateinit var userPubKey: String

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
        names = ["-p", "--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val ddcConsumer = buildConsumer(configOptions)

        if (decrypt) {
            getUserPiecesDecrypted(configOptions, ddcConsumer, userPubKey, from, to, fields)
        } else {
            getUserPiecesEncrypted(ddcConsumer, userPubKey, from, to, fields)
        }

        Thread.sleep(CONSUMING_SESSION_IN_MS)
    }

    private fun getUserPiecesDecrypted(
        configOptions: Map<String, String>,
        ddcConsumer: Consumer,
        userPubKey: String,
        from: Instant?,
        to: Instant?,
        fields: List<String> = listOf()
    ) {
        val encryptionConfig = ddcCliConfigFile.readEncryptionConfig(configOptions)
        val secretKey = CryptoSecretKey(encryptionConfig.masterEncryptionKey)

        ddcConsumer.getUserPieces(userPubKey, from?.toString() ?: "", to?.toString() ?: "", fields).subscribe().with(
            { piece ->
                runCatching {
                    piece.data = secretKey.decryptWithScopes(piece.data!!, encryptionConfig.encryptionJsonPaths)
                }.fold({ println(piece) }, { println("Can't decrypt data: " + piece.data) })
            },
            { e -> println(e) }
        )
    }

    private fun getUserPiecesEncrypted(
        ddcConsumer: Consumer,
        userPubKey: String,
        from: Instant?,
        to: Instant?,
        fields: List<String> = listOf()
    ) {
        ddcConsumer.getUserPieces(userPubKey, from?.toString() ?: "", to?.toString() ?: "", fields)
            .subscribe().with({ piece -> println(piece) }, { e -> println(e) })
    }
}
