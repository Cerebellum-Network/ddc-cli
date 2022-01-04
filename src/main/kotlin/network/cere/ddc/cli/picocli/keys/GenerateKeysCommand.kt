package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "generate-keys")
class GenerateKeysCommand : AbstractCommand() {

    @CommandLine.Option(
        names = ["--scheme"],
        required = true,
        description = ["Signature scheme: $SR_25519, $ED_25519, or $SECP_256_K_1"]
    )
    lateinit var scheme: String

    override fun run() {
        val mnemonicCode = MnemonicCode(WordCount.COUNT_12)
        val entropy = mnemonicCode.toEntropy()
        val keyPair = generateKeyPair(entropy, "mnemonic".toByteArray(), scheme)

        println("Secret phrase: " + mnemonicCode.joinToString(" "))
        println("Public key: ${keyPair.publicKey}")
        println("Private key: ${keyPair.privateKey}")
    }
}
