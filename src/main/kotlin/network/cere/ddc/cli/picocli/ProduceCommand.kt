package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.producer.Piece
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine
import java.time.Instant
import java.util.*

@CommandLine.Command(name = "produce")
class ProduceCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

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
        names = ["--encrypt"],
        description = ["Encrypt data"]
    )
    var encrypt: Boolean = false

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val producerConfig = ddcCliConfigFile.readProducerConfig(configOptions)
        val ddcProducer = buildProducer(producerConfig)

        if (encrypt) {
            val encryptionConfig = ddcCliConfigFile.readEncryptionConfig(configOptions)
            val appMasterEncryptionKey = CryptoSecretKey(encryptionConfig.masterEncryptionKey)
            data = appMasterEncryptionKey.encryptWithScopes(data, encryptionConfig.encryptionJsonPaths).encryptedData
        }

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
}
