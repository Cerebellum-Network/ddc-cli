package network.cere.ddc.cli.picocli.`object`

import picocli.CommandLine

@CommandLine.Command(
    name = "object-storage", subcommands = [
        StoreObjectCommand::class,
        ReadObjectCommand::class,
        ReadEdekCommand::class,
        StoreEdekCommand::class,
        BenchmarkCommand::class,
        GenerateLoadCommand::class
    ]
)
class ObjectStorageCommand
