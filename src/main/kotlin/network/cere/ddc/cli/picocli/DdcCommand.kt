package network.cere.ddc.cli.picocli

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import network.cere.ddc.cli.picocli.keys.ExtractKeysCommand
import network.cere.ddc.cli.picocli.keys.GenerateKeysCommand
import network.cere.ddc.cli.picocli.`object`.ObjectCommand
import picocli.CommandLine
import picocli.CommandLine.IFactory

@QuarkusMain
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        GenerateKeysCommand::class,
        ExtractKeysCommand::class,
        SignCommand::class,
        ConfigureCommand::class,
        BenchmarkCommand::class,
        GenerateLoadCommand::class,
        ObjectCommand::class,
        ConsumeCommand::class,
        GetAppPiecesCommand::class,
        GetPieceCommand::class,
        GetUserPiecesCommand::class,
        ProduceCommand::class,
        CreateAppCommand::class,
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
