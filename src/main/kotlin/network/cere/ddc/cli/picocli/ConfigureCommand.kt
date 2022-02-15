package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PRIV_KEY_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.GATEWAY_URL_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.SIGNATURE_SCHEME_CONFIG
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine
import java.net.URL

@CommandLine.Command(name = "configure")
class ConfigureCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--appPrivKey"],
        description = ["Application private key (required for producing)"]
    )
    var appPrivKey: String? = null

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

    override fun run() {
        val configOptions = mutableMapOf<String, String>()
        appPrivKey?.let {
            if (scheme.isNullOrEmpty()) {
                throw RuntimeException("scheme is required when updating appPrivKey")
            }
            configOptions.put(APP_PRIV_KEY_CONFIG, it)
        }
        scheme?.let { it ->
            if (!listOf(Scheme.ED_25519, Scheme.SR_25519, Scheme.SECP_256_K_1).contains(scheme)) {
                throw RuntimeException("Please provide a valid signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}")
            }
            configOptions.put(SIGNATURE_SCHEME_CONFIG, it)
        }
        gatewayUrl?.let { url ->
            runCatching { URL(url) }
                .onFailure { throw RuntimeException("Please provide a valid gateway url") }
                .onSuccess { configOptions.put(GATEWAY_URL_CONFIG, url) }
        }

        if (configOptions.isNotEmpty()) {
            ddcCliConfigFile.write(configOptions, profile)
        }
    }
}
