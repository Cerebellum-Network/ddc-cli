package network.cere.ddc.cli.picocli.keys

import io.emeraldpay.polkaj.schnorrkel.SchnorrkelNative
import network.cere.ddc.cli.picocli.AbstractCommand
import org.bitcoinj.crypto.MnemonicCode
import org.bouncycastle.util.encoders.Hex
import picocli.CommandLine

@CommandLine.Command(name = "extract-keys")
class ExtractKeysCommand() : AbstractCommand() {

    @CommandLine.Option(
        names = ["--secret-phrase"],
        description = ["List of secret words to generate keys"]
    )
    var secretPhrase: List<String> = listOf()

    override fun run() {
        val secretSeed = MnemonicCode.toSeed(secretPhrase, "")

        val keyPair = SchnorrkelNative().generateKeyPairFromSeed(secretSeed)
        println("Public key: " + Hex.toHexString(keyPair.publicKey))
        println("Private key: " + Hex.toHexString(keyPair.secretKey))
    }
}
