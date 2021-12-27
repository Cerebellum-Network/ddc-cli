package network.cere.ddc.cli.picocli.nft

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import picocli.CommandLine

@CommandLine.Command(name = "store-edek")
class StoreEdekCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--nft-id"],
        description = ["Nft Id where store asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-u", "--url"],
        description = ["Metadata url"],
        required = true
    )
    var url: String = ""

    @CommandLine.Option(
        names = ["-k", "--publicKey"],
        description = ["Public key Hex for EDEK"],
        required = true
    )
    var publicKey: String = ""

    @CommandLine.Option(
        names = ["-v", "--value"],
        description = ["EDEK value"],
        required = true
    )
    var value: String = ""

    override fun run() {
        val objectMapper = jacksonObjectMapper()
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val storedEdek = runBlocking {
            nftStorage.storeEdek(nftId, NftPath(url), Edek(publicKey = publicKey, value = value))
        }

        println("Stored EDEK: ${objectMapper.writeValueAsString(storedEdek)}")
    }
}
