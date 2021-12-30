package network.cere.ddc.cli.picocli.event

import picocli.CommandLine

@CommandLine.Command(name = "event-storage", subcommands = [CreateAppCommand::class])
class EventStorageCommand