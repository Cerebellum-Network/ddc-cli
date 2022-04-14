package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.CONTACT_ADDRESS_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.GATEWAY_URL_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.PRIVATE_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.SIGNATURE_SCHEME_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.WS_URL_CONFIG
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine
import java.net.URL

@CommandLine.Command(name = "configure")
class ConfigureCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--privateKey"],
        description = ["Private key (required for requests that require a signature)"]
    )
    var privateKey: String? = null

    @CommandLine.Option(
        names = ["--scheme"],
        description = ["Signature scheme (required for requests that require a signature)"]
    )
    var scheme: String? = null

    @CommandLine.Option(
        names = ["--gatewayUrl"],
        description = ["Gateway url"]
    )
    var gatewayUrl: String? = null

    @CommandLine.Option(
        names = ["--wsUrl"],
        description = ["Ws url"]
    )
    var wsUrl: String? = null

    @CommandLine.Option(
        names = ["--contractAddress"],
        description = ["Contract address"]
    )
    var contractAddress: String? = null

    override fun run() {
        val configOptions = mutableMapOf<String, String>()
        privateKey?.let {
            if (scheme.isNullOrEmpty()) {
                throw RuntimeException("scheme is required when updating privateKey")
            }
            configOptions[PRIVATE_KEY_CONFIG] = it
        }
        scheme?.let { it ->
            if (!listOf(Scheme.ED_25519, Scheme.SR_25519, Scheme.SECP_256_K_1).contains(scheme)) {
                throw RuntimeException("Please provide a valid signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}")
            }
            configOptions[SIGNATURE_SCHEME_CONFIG] = it
        }
        gatewayUrl?.let { url ->
            runCatching { URL(url) }
                .onFailure { throw RuntimeException("Please provide a valid gateway url") }
                .onSuccess { configOptions[GATEWAY_URL_CONFIG] = url }
        }

        wsUrl?.let { url ->
            runCatching { URL(url) }
                .onFailure { throw RuntimeException("Please provide a valid ws url") }
                .onSuccess { configOptions[WS_URL_CONFIG] = url }
        }

        contractAddress?.let { url ->
            runCatching { URL(url) }
                .onFailure { throw RuntimeException("Please provide a valid contract address") }
                .onSuccess { configOptions[CONTACT_ADDRESS_CONFIG] = url }
        }

        if (configOptions.isNotEmpty()) {
            ddcCliConfigFile.write(configOptions, profile)
        }
    }
}
