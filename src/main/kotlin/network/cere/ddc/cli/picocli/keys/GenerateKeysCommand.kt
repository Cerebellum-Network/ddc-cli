package network.cere.ddc.cli.picocli.keys

import io.emeraldpay.polkaj.schnorrkel.SchnorrkelNative
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.cli.picocli.toHex
import org.bitcoinj.crypto.MnemonicCode
import picocli.CommandLine
import java.security.SecureRandom

@CommandLine.Command(name = "generate-keys")
class GenerateKeysCommand() : AbstractCommand() {

    override fun run() {
        val code = MnemonicCode()
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)

        val secretPhrase = code.toMnemonic(entropy)

        val secretSeed = MnemonicCode.toSeed(secretPhrase, "")

        val keyPair = SchnorrkelNative().generateKeyPairFromSeed(secretSeed)
        println("Secret seed: " + secretSeed.toHex())
        println("Public key: " + keyPair.publicKey.toHex())
        println("Private key: " + keyPair.secretKey.toHex())
    }
}
