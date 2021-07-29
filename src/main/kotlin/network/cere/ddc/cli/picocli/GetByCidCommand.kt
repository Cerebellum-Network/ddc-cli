package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.crypto.v1.key.secret.CryptoSecretKey
import picocli.CommandLine

@CommandLine.Command(name = "get-by-cid")
class GetByCidCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

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

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val ddcConsumer = buildConsumer(configOptions)

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
