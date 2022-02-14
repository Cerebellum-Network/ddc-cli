package network.cere.ddc.cli.picocli.kv

import picocli.CommandLine

@CommandLine.Command(
    name = "kv", subcommands = [
        ReadCommand::class,
        StoreCommand::class
    ]
)
class KeyValueCommand
