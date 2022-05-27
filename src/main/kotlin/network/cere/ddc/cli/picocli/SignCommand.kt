package network.cere.ddc.cli.picocli

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine

@CommandLine.Command(name = "sign")
class SignCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand() {

    @CommandLine.Option(
        names = ["-s", "--scheme"],
        required = true,
        description = ["Signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}"]
    )
    lateinit var scheme: String

    @CommandLine.Option(names = ["-d", "--data"], required = true, description = ["Data to be signed"])
    lateinit var data: String

    override fun run() {
        val seed = ddcCliConfigFile.read(profile).let { ddcCliConfigFile.readSeed(it) }
        val signatureScheme = Scheme.create(scheme, seed)
        val signature = signatureScheme.sign(data.toByteArray())

        println("Public key: ${signatureScheme.publicKeyHex}")
        println("Signed data: $signature")
    }
}
