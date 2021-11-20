package network.cere.ddc.cli.picocli.keys

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.crypto.v1.toHex
import org.bitcoinj.crypto.MnemonicCode
import picocli.CommandLine
import java.security.SecureRandom

@CommandLine.Command(name = "generate-keys")
class GenerateKeysCommand : AbstractCommand() {

    companion object {
        private const val ENTROPY_LENGTH = 16
    }

    override fun run() {
        val code = MnemonicCode()
        val entropy = ByteArray(ENTROPY_LENGTH)
        SecureRandom().nextBytes(entropy)

        val secretPhrase = code.toMnemonic(entropy)

        val secretSeed = pbkdf2Seed(entropy, "mnemonic".toByteArray())
        val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)

        println("Secret phrase: " + secretPhrase.joinToString(" "))
        println("Public key: ${keyPair.publicKey.toPublicKey().toHex()}")
        println("Private key: ${secretSeed.toHex()}")
    }
}
