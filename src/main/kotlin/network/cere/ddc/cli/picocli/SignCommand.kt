package network.cere.ddc.cli.picocli

import io.emeraldpay.polkaj.schnorrkel.Schnorrkel
import io.emeraldpay.polkaj.schnorrkel.SchnorrkelNative
import org.bouncycastle.util.encoders.Hex
import picocli.CommandLine

@CommandLine.Command(name = "sign")
class SignCommand() : AbstractCommand() {

    @CommandLine.Option(
        names = ["-k", "--key", "--privateKey"],
        description = ["Private key to sign data"]
    )
    lateinit var privateKey: String

    @CommandLine.Option(names = ["-d", "--data"], required = true, description = ["Data to be signed"])
    lateinit var data: String

    override fun run() {
        val keyPair = Schnorrkel.getInstance().generateKeyPairFromSeed(privateKey.toByteArray())
        SchnorrkelNative.getInstance().sign(data.toByteArray(), keyPair)

        println("Public key: " + Hex.toHexString(keyPair.publicKey))
        println("Private key: " + Hex.toHexString(keyPair.secretKey))
    }
}
