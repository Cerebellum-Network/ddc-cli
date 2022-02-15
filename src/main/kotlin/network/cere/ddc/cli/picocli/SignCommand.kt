package network.cere.ddc.cli.picocli

import com.debuggor.schnorrkel.sign.SigningContext
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine

@CommandLine.Command(name = "sign")
class SignCommand : AbstractCommand() {

    private val signingContext = SigningContext.createSigningContext("substrate".toByteArray())

    @CommandLine.Option(
        names = ["-k", "--key", "--privateKey"],
        description = ["Private key to sign data"]
    )
    lateinit var privateKey: String

    @CommandLine.Option(
        names = ["-s", "--scheme"],
        required = true,
        description = ["Signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}"]
    )
    lateinit var scheme: String

    @CommandLine.Option(names = ["-d", "--data"], required = true, description = ["Data to be signed"])
    lateinit var data: String

    override fun run() {
        val signatureScheme = Scheme.create(scheme, privateKey)
        val signature = signatureScheme.sign(data.toByteArray())


        println("Public key: ${signatureScheme.publicKeyHex}")
        println("Private key: $privateKey")
        println("Signed data: $signature")
    }
}
