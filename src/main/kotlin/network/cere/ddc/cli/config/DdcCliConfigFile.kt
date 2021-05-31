package network.cere.ddc.cli.config

import java.io.File
import javax.enterprise.context.ApplicationScoped
import kotlin.streams.asSequence

@ApplicationScoped
class DdcCliConfigFile(private val ddcCliConfigFilePath: String = DDC_CLI_CONFIG_FILE_PATH) {
    companion object {
        const val APP_PUB_KEY_CONFIG = "appPubKey"
        const val APP_PRIV_KEY_CONFIG = "appPrivKey"
        const val BOOTSTRAP_NODES_CONFIG = "bootstrapNodes"
        const val PARTITION_POLL_INTERVAL_MS_CONFIG = "partitionPollIntervalMs"

        private const val DDC_CLI_CONFIG_FILE_PATH = ".ddc/test-cli-config"
    }

    fun read(): Map<String, String> {
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        return if (ddcCliConfigFile.exists()) {
            ddcCliConfigFile.bufferedReader().lines().asSequence()
                .filter { it.isNotEmpty() }
                .map { it.split("=") }
                .map { it[0] to it[1] }
                .toMap()
        } else {
            ddcCliConfigFile.parentFile.mkdirs()
            ddcCliConfigFile.createNewFile()
            mapOf()
        }
    }

    fun write(configOptions: Map<String, String>) {
        val existingConfigOptions = read()
        val updatedConfigOptions = existingConfigOptions.toMutableMap()
        updatedConfigOptions.putAll(configOptions)

        val ddcCliConfigFile = File(ddcCliConfigFilePath)

        updatedConfigOptions
            .map { option -> option.key + "=" + option.value }
            .joinToString(separator = "\n")
            .let { ddcCliConfigFile.writeText(it) }
    }
}