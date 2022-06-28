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
        names = ["-s", "--scheme"],
        required = true,
        description = ["Signature scheme: ${Scheme.SR_25519} or ${Scheme.ED_25519}"]
    )
    lateinit var scheme: String

    override fun run() {
        val keyPair = generateKeyPair(Mnemonics.MnemonicCode(secretPhrase), "mnemonic", scheme)

        println("Secret phrase: $secretPhrase")
        println("Secret seed: ${keyPair.secretSeed}")
        println("Public key: ${keyPair.publicKey}")
        println("SS58 Address: ${publicKeyToSS58Address(keyPair.publicKey)}")
    }
}
