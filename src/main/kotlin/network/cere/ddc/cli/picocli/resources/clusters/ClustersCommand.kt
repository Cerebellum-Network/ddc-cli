package network.cere.ddc.cli.picocli.resources.clusters

import picocli.CommandLine

@CommandLine.Command(
    name = "clusters", subcommands = [
        GetCommand::class,
    ]
)
class ClustersCommand