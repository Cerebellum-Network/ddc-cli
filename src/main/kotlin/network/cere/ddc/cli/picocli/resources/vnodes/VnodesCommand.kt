package network.cere.ddc.cli.picocli.resources.vnodes

import picocli.CommandLine

@CommandLine.Command(
    name = "vnodes", subcommands = [
        GetCommand::class,
        RecoverCommand::class,
    ]
)
class VnodesCommand
