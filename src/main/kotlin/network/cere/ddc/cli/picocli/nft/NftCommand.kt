package network.cere.ddc.cli.picocli.nft

import picocli.CommandLine

@CommandLine.Command(
    name = "nft-storage", subcommands = [
        StoreAssetCommand::class,
        ReadAssetCommand::class,
        ReadMetadataCommand::class,
        StoreMetadataCommand::class,
        ReadEdekCommand::class,
        StoreEdekCommand::class,
        BenchmarkCommand::class,
        GenerateLoadCommand::class
    ]
)
class NftCommand {

    companion object {
        const val ERC_721 = "ERC-721"
        const val ERC_1155 = "ERC-1155"
    }

}
