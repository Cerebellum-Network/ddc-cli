package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.KeyValueStorage
import picocli.CommandLine

abstract class AbstractCommand(private val ddcCliConfigFile: DdcCliConfigFile = DdcCliConfigFile()) : Runnable {

    @CommandLine.Option(
        names = ["-p", "--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    fun buildContentAddressableStorage(configOptions: Map<String, String>): ContentAddressableStorage {
        val cfg = readStorageConfiguration(configOptions)
        return ContentAddressableStorage(Scheme.create(cfg.scheme, cfg.seed), cfg.cdnUrl)
    }

    fun buildKeyValueStorage(configOptions: Map<String, String>): KeyValueStorage {
        val cfg = readStorageConfiguration(configOptions)
        return KeyValueStorage(Scheme.create(cfg.scheme, cfg.seed), cfg.cdnUrl)
    }

    private fun readStorageConfiguration(configOptions: Map<String, String>) = StorageConfiguration(
        seed = ddcCliConfigFile.readSeed(configOptions),
        scheme = ddcCliConfigFile.readSignatureScheme(configOptions),
        cdnUrl = ddcCliConfigFile.readCdnUrl(configOptions)
    )

    private data class StorageConfiguration(val seed: String, val scheme: String, val cdnUrl: String)
}
