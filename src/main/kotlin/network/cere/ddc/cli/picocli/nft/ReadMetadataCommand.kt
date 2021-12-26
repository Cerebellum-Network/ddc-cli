package network.cere.ddc.cli.picocli.nft

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.cli.picocli.nft.NftCommand.Companion.ERC_1155
import network.cere.ddc.cli.picocli.nft.NftCommand.Companion.ERC_721
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import picocli.CommandLine

@CommandLine.Command(name = "read-metadata")
class ReadMetadataCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--nft-id"],
        description = ["Nft Id where stored required asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-s", "--schema"],
        description = ["ERC-1155 or ERC-721 metadata JSON schema"],
    )
    var schema: String = ERC_721

    @CommandLine.Option(
        names = ["--url"],
        description = ["Url where stored required asset"],
        required = true
    )
    var url: String = ""

    override fun run() {
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val metadata = runBlocking { nftStorage.readMetadata(nftId, NftPath(url)) }

        println("Metadata: ${jacksonObjectMapper().writeValueAsString(metadata)}")
    }
}
