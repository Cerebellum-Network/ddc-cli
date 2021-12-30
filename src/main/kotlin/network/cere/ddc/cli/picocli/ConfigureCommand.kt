package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PRIV_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PUB_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODE_IDS_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.ENCRYPTION_JSON_PATHS_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.MASTER_ENCRYPTION_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.PARTITION_POLL_INTERVAL_MS_CONFIG
import picocli.CommandLine

@CommandLine.Command(name = "configure")
class ConfigureCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--appPubKey"],
        description = ["Application public key (required for both producing and consuming)"]
    )
    var appPubKey: String? = null

    @CommandLine.Option(
        names = ["--appPrivKey"],
        description = ["Application private key (required for producing)"]
    )
    var appPrivKey: String? = null

    @CommandLine.Option(
        names = ["--bootstrapNodes"],
        description = ["List of bootstrap nodes (required for both producing and consuming)"]
    )
    var bootstrapNodes: List<String>? = null

    @CommandLine.Option(
        names = ["--bootstrapNodeIds"],
        description = ["List of bootstrap node ids in same order as bootstrapNodes (required for Object storage)"]
    )
    var bootstrapNodeIds: List<String>? = null

    @CommandLine.Option(
        names = ["--partitionPollIntervalMs"],
        description = ["Partition poll interval in ms"]
    )
    var partitionPollIntervalMs: String? = null

    @CommandLine.Option(
        names = ["--masterEncryptionKey"],
        description = ["Master encryption key to use for encryption/decryption"]
    )
    var masterEncryptionKey: String? = null

    @CommandLine.Option(
        names = ["--encryptionJsonPaths"],
        description = ["Json paths to encrypt/decrypt"]
    )
    var encryptionJsonPaths: List<String>? = null

    override fun run() {
        val configOptions = mutableMapOf<String, String>()

        appPubKey?.let { configOptions.put(APP_PUB_KEY_CONFIG, it) }
        appPrivKey?.let { configOptions.put(APP_PRIV_KEY_CONFIG, it) }
        bootstrapNodes?.let { nodes -> configOptions.put(BOOTSTRAP_NODES_CONFIG, nodes.joinToString()) }
        bootstrapNodeIds?.let { ids -> configOptions.put(BOOTSTRAP_NODE_IDS_CONFIG, ids.joinToString()) }
        partitionPollIntervalMs?.let { configOptions.put(PARTITION_POLL_INTERVAL_MS_CONFIG, it) }
        masterEncryptionKey?.let { configOptions.put(MASTER_ENCRYPTION_KEY_CONFIG, it) }
        encryptionJsonPaths?.let { it -> configOptions.put(ENCRYPTION_JSON_PATHS_CONFIG, it.joinToString()) }

        if (configOptions.isNotEmpty()) {
            ddcCliConfigFile.write(configOptions, profile)
        }
    }
}
