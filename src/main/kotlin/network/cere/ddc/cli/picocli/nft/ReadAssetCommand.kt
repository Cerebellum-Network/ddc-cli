package network.cere.ddc.cli.picocli.nft

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.nft.model.NftPath
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "read-asset")
class ReadAssetCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--nft-id"],
        description = ["Nft Id where stored required asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-u", "--url"],
        description = ["Url where stored required asset"],
        required = true
    )
    var url: String = ""

    override fun run() {
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val data = runBlocking { nftStorage.readAsset(nftId, NftPath(url)) }

        println("Data: ${String(Base64.getEncoder().encode(data))}")
    }
}
