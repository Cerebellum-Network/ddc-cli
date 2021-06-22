package network.cere.ddc.cli.picocli

import io.quarkus.picocli.runtime.annotations.TopCommand
import picocli.CommandLine

@TopCommand
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        CreateAppCommand::class,
        ConfigureCommand::class,
        ProduceCommand::class,
        ConsumeCommand::class,
        GenerateLoadCommand::class
    ]
)
class DdcCommand
