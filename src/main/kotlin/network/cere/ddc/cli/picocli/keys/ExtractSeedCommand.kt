package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine

@CommandLine.Command(name = "extract-seed")
class ExtractSeedCommand : AbstractCommand() {

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
        println(generateSeedHex(Mnemonics.MnemonicCode(secretPhrase), "mnemonic", scheme))
    }
}
