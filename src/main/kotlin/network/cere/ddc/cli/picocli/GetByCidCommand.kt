package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine

@CommandLine.Command(name = "consume")
class GetByCidCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

    @CommandLine.Option(
        names = ["-u", "--user", "--userPubKey"],
        required = true,
        description = ["User public key to specify in data piece"],
    )
    lateinit var userPubKey: String

    @CommandLine.Option(
        names = ["-c", "--cid"],
        required = true,
        description = ["Cid of data piece"]
    )
    lateinit var cid: String

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

        val piece = ddcConsumer.getByCid(userPubKey, cid).await().indefinitely()

        if (decrypt) {
            val encryptionConfig = ddcCliConfigFile.readEncryptionConfig(configOptions)
            val appMasterEncryptionKey = CryptoSecretKey(encryptionConfig.masterEncryptionKey)

            try {
                piece.data =
                    appMasterEncryptionKey.decryptWithScopes(piece.data!!, encryptionConfig.encryptionJsonPaths)
                println(piece)
            } catch (e: Exception) {
                println("Can't decrypt data: " + piece.data)
            }
        } else {
            println(piece)
        }
    }
}
