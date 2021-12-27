package network.cere.ddc.cli.picocli.nft

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.cli.picocli.nft.NftCommand.Companion.ERC_1155
import network.cere.ddc.cli.picocli.nft.NftCommand.Companion.ERC_721
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import picocli.CommandLine

@CommandLine.Command(name = "store-metadata")
class StoreMetadataCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {


    @CommandLine.Option(
        names = ["-i", "--nft-id"],
        description = ["Nft Id where store asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-s", "--schema"],
        description = ["ERC-1155 or ERC-721 metadata JSON schema"],
    )
    var schema: String = ERC_721

    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Metadata to be stored in Nft Storage (ERC-1155 JSON format)"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val nftStorage = buildNftStorage(ddcCliConfigFile.read(profile))
        val metadata = when (schema) {
            ERC_721 -> jacksonObjectMapper().readValue<Erc721Metadata>(data)
            ERC_1155 -> jacksonObjectMapper().readValue<Erc1155Metadata>(data)
            else -> throw IllegalArgumentException("Invalid schema name $schema")
        }

        val nftPath = runBlocking { nftStorage.storeMetadata(nftId, metadata) }

        println("Url: ${nftPath.url}")
    }
}
