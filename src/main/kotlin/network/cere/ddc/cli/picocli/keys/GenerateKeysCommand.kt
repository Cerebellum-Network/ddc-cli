package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine

@CommandLine.Command(name = "generate-keys")
class GenerateKeysCommand : AbstractCommand() {

    @CommandLine.Option(
        names = ["-s", "--scheme"],
        required = true,
        description = ["Signature scheme: ${Scheme.SR_25519} or ${Scheme.ED_25519}"]
    )
    lateinit var scheme: String

    override fun run() {
        val mnemonicCode = MnemonicCode(WordCount.COUNT_12)
        val seedHex = generateSeedHex(mnemonicCode, "mnemonic", scheme)

        println("Secret phrase: " + mnemonicCode.joinToString(" "))
        println("Seed hex: $seedHex")
    }
}
