package network.cere.ddc.cli.config

import java.io.File
import javax.enterprise.context.ApplicationScoped
import kotlin.streams.asSequence

@ApplicationScoped
class DdcCliConfigFile(private var ddcCliConfigFilePath: String? = null) {
    companion object {
        const val APP_PUB_KEY_CONFIG = "appPubKey"
        const val APP_PRIV_KEY_CONFIG = "appPrivKey"
        const val BOOTSTRAP_NODES_CONFIG = "bootstrapNodes"
        const val PARTITION_POLL_INTERVAL_MS_CONFIG = "partitionPollIntervalMs"

        const val DEFAULT_PROFILE = "default"
    }

    private val defaultDdcCliConfigFilePath = System.getProperty("user.home") + "/.ddc/cli-config"

    fun read(profile: String? = DEFAULT_PROFILE): Map<String, String> {
        return readAllProfiles().getOrDefault(profile, mapOf())
    }

    fun write(configOptions: Map<String, String>, profile: String? = DEFAULT_PROFILE) {
        val profileToConfigOptions = readAllProfiles()
        val existingConfigOptions = profileToConfigOptions.getOrPut(profile!!) { mutableMapOf() }
        existingConfigOptions.putAll(configOptions)

        val ddcCliConfigFile = File(ddcCliConfigFilePath ?: defaultDdcCliConfigFilePath)

        profileToConfigOptions
            .flatMap { options ->
                mutableListOf("[${options.key}]") + options.value.map { option -> option.key + "=" + option.value }
            }
            .joinToString("\n")
            .let { ddcCliConfigFile.writeText(it) }
    }

    private fun readAllProfiles(): MutableMap<String, MutableMap<String, String>> {
        val ddcCliConfigFile = File(ddcCliConfigFilePath ?: defaultDdcCliConfigFilePath)
        return if (ddcCliConfigFile.exists()) {
            val profileToConfigOptions = mutableMapOf<String, MutableMap<String, String>>()

            var profile = ""
            ddcCliConfigFile.bufferedReader().lines().asSequence()
                .filter { it.isNotEmpty() }
                .map { it.trim() }
                .forEach { line ->
                    if (line.startsWith("[") && line.endsWith("]")) {
                        profile = line.trimStart('[').trimEnd(']')
                    } else {
                        val option = line.split("=")
                        val configOptions = profileToConfigOptions.getOrPut(profile) { mutableMapOf() }
                        configOptions[option[0]] = option[1]
                    }
                }
            return profileToConfigOptions
        } else {
            ddcCliConfigFile.parentFile.mkdirs()
            ddcCliConfigFile.createNewFile()
            mutableMapOf()
        }
    }
}