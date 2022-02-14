package network.cere.ddc.cli.picocli.ca

import picocli.CommandLine

@CommandLine.Command(
    name = "ca", subcommands = [
        StoreCommand::class,
        ReadCommand::class,
        SearchCommand::class
    ]
)
class ContentAddressableCommand
