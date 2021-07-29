package network.cere.ddc.cli.picocli

import picocli.CommandLine
import io.quarkus.runtime.QuarkusApplication

import io.quarkus.runtime.annotations.QuarkusMain
import picocli.CommandLine.IFactory
import java.lang.Exception

@QuarkusMain
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        BenchmarkCommand::class,
        ConfigureCommand::class,
        ConsumeCommand::class,
        CreateAppCommand::class,
        GenerateLoadCommand::class,
        GetAppPiecesCommand::class,
        GetByCidCommand::class,
        GetUserPiecesCommand::class,
        ProduceCommand::class,
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    @Throws(Exception::class)
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
