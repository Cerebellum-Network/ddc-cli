package network.cere.ddc.cli.config

import network.cere.ddc.client.consumer.ConsumerConfig
import network.cere.ddc.client.producer.ProducerConfig
import network.cere.ddc.core.model.Node
import java.io.File
import javax.enterprise.context.ApplicationScoped
import kotlin.streams.asSequence

@ApplicationScoped
class DdcCliConfigFile(private var ddcCliConfigFilePath: String? = null) {
    companion object {
        const val APP_PUB_KEY_CONFIG = "appPubKey"
        const val APP_PRIV_KEY_CONFIG = "appPrivKey"
        const val BOOTSTRAP_NODES_CONFIG = "bootstrapNodes"
        const val BOOTSTRAP_NODE_IDS_CONFIG = "bootstrapNodeIds"
        const val PARTITION_POLL_INTERVAL_MS_CONFIG = "partitionPollIntervalMs"
        const val MASTER_ENCRYPTION_KEY_CONFIG = "masterEncryptionKey"
        const val ENCRYPTION_JSON_PATHS_CONFIG = "encryptionJsonPaths"

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

    fun readEncryptionConfig(configOptions: Map<String, String>): EncryptionConfig {
        val masterEncryptionKey = configOptions[MASTER_ENCRYPTION_KEY_CONFIG]
        if (masterEncryptionKey == null || masterEncryptionKey.isEmpty()) {
            throw RuntimeException("Missing required parameter masterEncryptionKey. Please use 'configure' command.")
        }

        val encryptionJsonPathsAsString = configOptions[ENCRYPTION_JSON_PATHS_CONFIG]
        if (encryptionJsonPathsAsString == null || encryptionJsonPathsAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter encryptionJsonPaths. Please use 'configure' command.")
        }

        return EncryptionConfig(masterEncryptionKey, encryptionJsonPathsAsString.split(","))
    }

    fun readProducerConfig(configOptions: Map<String, String>): ProducerConfig {
        val appPubKey = configOptions[APP_PUB_KEY_CONFIG]
        if (appPubKey == null || appPubKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPubKey. Please use 'configure' command.")
        }

        val appPrivKey = configOptions[APP_PRIV_KEY_CONFIG]
        if (appPrivKey == null || appPrivKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPrivKey. Please use 'configure' command.")
        }

        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        return ProducerConfig(
            appPubKey = appPubKey,
            appPrivKey = appPrivKey,
            bootstrapNodes = bootstrapNodesAsString.split(",")
        )
    }

    fun readObjectStorageTrustedNodes(configOptions: Map<String, String>): List<Node> {
        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        val bootstrapNodeIdsAsString = configOptions[BOOTSTRAP_NODE_IDS_CONFIG]
        if (bootstrapNodeIdsAsString == null || bootstrapNodeIdsAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodeIds. Please use 'configure' command.")
        }

        val bootstrapNodeAddresses = bootstrapNodesAsString.split(",")
        val bootstrapNodeIds = bootstrapNodeIdsAsString.split(",")
        if (bootstrapNodeAddresses.size != bootstrapNodeIds.size) {
            throw RuntimeException("Number bootstrap nodes and ids should be same. Please use 'configure' command.")
        }

        val trustedNodes = bootstrapNodeAddresses.zip(bootstrapNodeIds).map { Node(address = it.first, id = it.second) }

        return trustedNodes
    }

    fun readPrivateKey(configOptions: Map<String, String>): String {
        val appPrivKey = configOptions[APP_PRIV_KEY_CONFIG]
        if (appPrivKey == null || appPrivKey.isEmpty()) {
            throw RuntimeException("Missing required parameter appPrivKey. Please use 'configure' command.")
        }

        return appPrivKey
    }

    fun readConsumerConfig(configOptions: Map<String, String>): ConsumerConfig {
        val appPubKey = configOptions[APP_PUB_KEY_CONFIG]
        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        val partitionPollIntervalMsAsString = configOptions[PARTITION_POLL_INTERVAL_MS_CONFIG]

        when {
            appPubKey.isNullOrEmpty() -> throw RuntimeException("Missing required parameter appPubKey. Please use 'configure' command.")
            bootstrapNodesAsString.isNullOrEmpty() -> throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
            !partitionPollIntervalMsAsString.isNullOrEmpty() -> {
                val partitionPollIntervalMs = partitionPollIntervalMsAsString.toInt()
                return ConsumerConfig(appPubKey, bootstrapNodesAsString.split(","), partitionPollIntervalMs)
            }
        }

        return ConsumerConfig(appPubKey!!, bootstrapNodesAsString!!.split(","))
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