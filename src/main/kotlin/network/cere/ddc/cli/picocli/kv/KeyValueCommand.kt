package network.cere.ddc.cli.picocli.kv

import picocli.CommandLine

@CommandLine.Command(
    name = "kv", subcommands = [
        DeleteCommand::class,
        ReadCommand::class,
        StoreCommand::class
    ]
)
class KeyValueCommand
