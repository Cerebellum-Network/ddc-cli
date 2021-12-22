package network.cere.ddc.cli.picocli.behaviour

import picocli.CommandLine

@CommandLine.Command(
    name = "behaviour-storage", subcommands = [
        ConsumeCommand::class,
        GetAppPiecesCommand::class,
        GetPieceCommand::class,
        GetUserPiecesCommand::class,
        ProduceCommand::class,
        CreateAppCommand::class,
    ]
)
class BehaviourCommand