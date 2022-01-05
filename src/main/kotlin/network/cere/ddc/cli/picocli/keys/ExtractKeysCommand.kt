package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.core.signature.Scheme
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
        description = ["Signature scheme: ${Scheme.SR_25519}, ${Scheme.ED_25519}, or ${Scheme.SECP_256_K_1}"]
    )
    lateinit var scheme: String

    override fun run() {
        val entropy = Mnemonics.MnemonicCode(secretPhrase).toEntropy()
        val keyPairSeed = generateKeyPair(entropy, "mnemonic".toByteArray(), scheme)

        println("Public key: ${keyPairSeed.publicKey}")
        println("Private key: ${keyPairSeed.secretSeed}")
    }
}
