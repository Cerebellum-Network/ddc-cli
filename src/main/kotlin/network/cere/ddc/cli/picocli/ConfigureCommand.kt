package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PRIV_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PUB_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.PARTITION_POLL_INTERVAL_MS_CONFIG
import picocli.CommandLine

@CommandLine.Command(name = "configure")
class ConfigureCommand(private val ddcCliConfigFile: DdcCliConfigFile) : Runnable {

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
        names = ["--partitionPollIntervalMs"],
        description = ["Partition poll interval in ms"]
    )
    var partitionPollIntervalMs: String? = null

    @CommandLine.Option(
        names = ["--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    override fun run() {
        val configOptions = mutableMapOf<String, String>()

        appPubKey?.let { configOptions.put(APP_PUB_KEY_CONFIG, it) }
        appPrivKey?.let { configOptions.put(APP_PRIV_KEY_CONFIG, it) }
        bootstrapNodes?.let { nodes ->
            configOptions.put(
                BOOTSTRAP_NODES_CONFIG,
                nodes.joinToString()
            )
        }
        partitionPollIntervalMs?.let { configOptions.put(PARTITION_POLL_INTERVAL_MS_CONFIG, it) }

        if (configOptions.isNotEmpty()) {
            ddcCliConfigFile.write(configOptions, profile)
        }
    }
}
