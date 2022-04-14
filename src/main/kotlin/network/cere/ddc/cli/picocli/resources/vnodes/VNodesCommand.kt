package network.cere.ddc.cli.picocli.resources.vnodes

import picocli.CommandLine

@CommandLine.Command(
    name = "vnodes", subcommands = [
        GetCommand::class,
        K1Command::class,
        K2Command::class,
    ]
)
class VNodesCommand
