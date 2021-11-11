package network.cere.ddc.cli.picocli.keys

import io.emeraldpay.polkaj.schnorrkel.SchnorrkelNative
import network.cere.ddc.cli.picocli.AbstractCommand
import org.bitcoinj.crypto.MnemonicCode
import org.bouncycastle.util.encoders.Hex
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
        println("Secret seed: " + Hex.toHexString(secretSeed))
        println("Public key: " + Hex.toHexString(keyPair.publicKey))
        println("Private key: " + Hex.toHexString(keyPair.secretKey))
    }
}
