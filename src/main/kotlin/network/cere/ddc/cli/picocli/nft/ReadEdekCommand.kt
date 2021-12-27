package network.cere.ddc.cli.picocli.nft

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.nft.model.NftPath
import picocli.CommandLine

@CommandLine.Command(name = "read-edek")
class ReadEdekCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--nft-id"],
        description = ["Nft Id where stored edek"],
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
        description = ["EDEK public key Hex"],
        required = true
    )
    var publicKey: String = ""

    override fun run() {
        val objectMapper = jacksonObjectMapper()
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val edek = runBlocking { nftStorage.readEdek(nftId, NftPath(url), publicKey) }

        println(objectMapper.writeValueAsString(edek))
    }
}
