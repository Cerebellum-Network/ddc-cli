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
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = ddcCliConfigFile.readSignatureScheme(configOptions)
        val gatewayUrl = ddcCliConfigFile.readGatewayUrl(configOptions)

        return ContentAddressableStorage(Scheme.create(scheme, privateKey), gatewayUrl)
    }

    fun buildKeyValueStorage(configOptions: Map<String, String>): KeyValueStorage {
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = ddcCliConfigFile.readSignatureScheme(configOptions)
        val gatewayUrl = ddcCliConfigFile.readGatewayUrl(configOptions)

        return KeyValueStorage(Scheme.create(scheme, privateKey), gatewayUrl)
    }
}
