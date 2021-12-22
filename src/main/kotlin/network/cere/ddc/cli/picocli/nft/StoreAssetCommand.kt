package network.cere.ddc.cli.picocli.nft

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "store-asset")
class StoreAssetCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--nft-id"],
        description = ["Nft Id where store asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Data to be stored in Nft Storage (Base64 format)"],
        required = true
    )
    var data: String = ""

    @CommandLine.Option(
        names = ["-n", "--name"],
        description = ["Data name to be stored"],
        required = true
    )
    var name: String = ""

    override fun run() {
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val nftPath = runBlocking { nftStorage.storeAsset(nftId, Base64.getDecoder().decode(data), name) }

        println("Url: ${nftPath.url}")
    }
}
