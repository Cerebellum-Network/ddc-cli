package network.cere.ddc.cli.picocli

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import picocli.CommandLine

import com.debuggor.schnorrkel.sign.SigningContext
import network.cere.ddc.crypto.v1.toHex


@CommandLine.Command(name = "sign")
class SignCommand : AbstractCommand() {

    @CommandLine.Option(
        names = ["-k", "--key", "--privateKey"],
        description = ["Private key to sign data"]
    )
    lateinit var privateKey: String

    @CommandLine.Option(names = ["-d", "--data"], required = true, description = ["Data to be signed"])
    lateinit var data: String

    override fun run() {
        val keyPair = KeyPair.fromSecretSeed(privateKey.toByteArray(), ExpansionMode.Ed25519)

        val signature = keyPair.sign(SigningContext.createSigningContext(ByteArray(0)).bytes(data.toByteArray()))

        println("Public key: ${keyPair.publicKey.toPublicKey().toHex()}")
        println("Private key: $privateKey")
        println("Signed data: ${signature.to_bytes().toHex()}")
    }
}
