package network.cere.ddc.cli.picocli.keys

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.crypto.v1.hexToBytes
import network.cere.ddc.crypto.v1.key.sign.signingKeyPairFromMnemonic
import network.cere.ddc.crypto.v1.toHex
import org.bitcoinj.crypto.MnemonicCode
import picocli.CommandLine

@CommandLine.Command(name = "extract-keys")
class ExtractKeysCommand : AbstractCommand() {

    @CommandLine.Option(
        names = ["--secret-phrase"],
        description = ["Secret phrase (mnemonic)"]
    )
    lateinit var secretPhrase: String

    override fun run() {
        val entropy = MnemonicCode.INSTANCE.toEntropy(secretPhrase.split(" ")).toHex()

        val secretSeed = pbkdf2Seed(entropy.hexToBytes(), "mnemonic".toByteArray())
        val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)

        println("Public key: ${keyPair.publicKey.toPublicKey().toHex()}")
        println("Private key: ${secretSeed.toHex()}")
    }
}
