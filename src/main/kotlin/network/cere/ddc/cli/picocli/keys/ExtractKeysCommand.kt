package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "extract-keys")
class ExtractKeysCommand : AbstractCommand() {

    @CommandLine.Option(
        names = ["--secret-phrase"],
        required = true,
        description = ["Secret phrase (mnemonic)"]
    )
    lateinit var secretPhrase: String

    @CommandLine.Option(
        names = ["--scheme"],
        description = ["Signature scheme used to generate the keys: $SR_25519, $ED_25519, or $SECP_256_K_1"]
    )
    lateinit var scheme: String

    override fun run() {
        val entropy = Mnemonics.MnemonicCode(secretPhrase).toEntropy()
        val keyPair = generateKeyPair(entropy, "mnemonic".toByteArray(), scheme)

        println("Public key: ${keyPair.publicKey}")
        println("Private key: ${keyPair.privateKey}")
    }
}
