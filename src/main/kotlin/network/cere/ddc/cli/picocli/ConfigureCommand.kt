package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.CDN_URL_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.SEED
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.SIGNATURE_SCHEME_CONFIG
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine
import java.net.URL

@CommandLine.Command(name = "configure")
class ConfigureCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--seed", "--key", "--appPrivKey"],
        description = ["Application seed/private key (required for producing)"]
    )
    var seed: String? = null

    @CommandLine.Option(
        names = ["--scheme"],
        description = ["Signature scheme (required for requests that require a signature)"]
    )
    var scheme: String? = null

    @CommandLine.Option(
        names = ["--cdnUrl"],
        description = ["CDN url"]
    )
    var cdnUrl: String? = null

    override fun run() {
        val configOptions = mutableMapOf<String, String>()
        seed?.let {
            if (scheme.isNullOrEmpty()) {
                throw RuntimeException("scheme is required when updating seed")
            }
            configOptions.put(SEED, it)
        }
        scheme?.let { it ->
            if (!listOf(Scheme.ED_25519, Scheme.SR_25519, Scheme.SECP_256_K_1).contains(scheme)) {
                throw RuntimeException("Please provide a valid signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}")
            }
            configOptions.put(SIGNATURE_SCHEME_CONFIG, it)
        }
        cdnUrl?.let { url ->
            runCatching { URL(url) }
                .onFailure { throw RuntimeException("Please provide a valid CDN url") }
                .onSuccess { configOptions.put(CDN_URL_CONFIG, url) }
        }

        if (configOptions.isNotEmpty()) {
            ddcCliConfigFile.write(configOptions, profile)
        }
    }
}
