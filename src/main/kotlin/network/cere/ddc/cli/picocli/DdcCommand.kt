package network.cere.ddc.cli.picocli

import picocli.CommandLine
import io.quarkus.runtime.QuarkusApplication

import io.quarkus.runtime.annotations.QuarkusMain
import picocli.CommandLine.IFactory

@QuarkusMain
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        CreateAppCommand::class,
        ConfigureCommand::class,
        ProduceCommand::class,
        ConsumeCommand::class,
        GetAppPiecesCommand::class,
        GetUserPiecesCommand::class,
        GetByCidCommand::class,
        GenerateLoadCommand::class
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}