package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.crypto.v1.toHex
import picocli.CommandLine

@CommandLine.Command(name = "generate-keys")
class GenerateKeysCommand : AbstractCommand() {

    override fun run() {
        val mnemonicCode = MnemonicCode(WordCount.COUNT_12)
        val entropy = mnemonicCode.toEntropy()

        val secretSeed = pbkdf2Seed(entropy, "mnemonic".toByteArray())
        val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)

        println("Secret phrase: " + mnemonicCode.joinToString(" "))
        println("Public key: ${keyPair.publicKey.toPublicKey().toHex()}")
        println("Private key: ${secretSeed.toHex()}")
    }
}
