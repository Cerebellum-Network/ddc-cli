package network.cere.ddc.cli.config

import java.io.File
import javax.enterprise.context.ApplicationScoped
import kotlin.streams.asSequence

@ApplicationScoped
class DdcCliConfigFile(private var ddcCliConfigFilePath: String? = null) {
    companion object {
        const val SEED = "seed"
        const val SIGNATURE_SCHEME_CONFIG = "scheme"
        const val GATEWAY_URL_CONFIG = "gatewayUrl"

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

    fun readSeed(configOptions: Map<String, String>): String {
        val seed = configOptions[SEED]
        if (seed.isNullOrEmpty()) {
            throw RuntimeException("Missing required parameter seed. Please use 'configure' command.")
        }

        return seed
    }

    fun readSignatureScheme(configOptions: Map<String, String>): String {
        val scheme = configOptions[SIGNATURE_SCHEME_CONFIG]
        if (scheme.isNullOrEmpty()) {
            throw RuntimeException("Missing required parameter 'scheme'. Please use 'configure' command.")
        }

        return scheme
    }

    fun readGatewayUrl(configOptions: Map<String, String>): String {
        val gatewayUrl = configOptions[GATEWAY_URL_CONFIG]
        if (gatewayUrl.isNullOrEmpty()) {
            throw RuntimeException("Missing required parameter 'gatewayUrl'. Please use 'configure' command.")
        }

        return gatewayUrl
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