package network.cere.ddc.cli.picocli.nft

import picocli.CommandLine

@CommandLine.Command(name = "nft-storage", subcommands = [StoreAssetCommand::class, ReadAssetCommand::class])
class NftCommand
