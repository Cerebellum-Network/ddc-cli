package network.cere.ddc.cli.picocli.resources.nodes

import picocli.CommandLine

@CommandLine.Command(
    name = "nodes", subcommands = [
        GetCommand::class,
    ]
)
class NodesCommand
