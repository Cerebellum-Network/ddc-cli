package network.cere.ddc.cli.picocli

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import network.cere.ddc.cli.picocli.ca.ContentAddressableCommand
import network.cere.ddc.cli.picocli.keys.ExtractSeedCommand
import network.cere.ddc.cli.picocli.keys.GenerateKeysCommand
import network.cere.ddc.cli.picocli.kv.KeyValueCommand
import picocli.CommandLine
import picocli.CommandLine.IExecutionExceptionHandler
import picocli.CommandLine.IFactory

@QuarkusMain
@CommandLine.Command(
    version = ["2.0.2.Prototype"],
    mixinStandardHelpOptions = true,
    subcommands = [
        GenerateKeysCommand::class,
        ExtractSeedCommand::class,
        SignCommand::class,
        ConfigureCommand::class,
        ContentAddressableCommand::class,
        KeyValueCommand::class
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        val errorHandler = IExecutionExceptionHandler { ex, cmd, _ ->
            cmd.err.println(cmd.colorScheme.errorText("Error: ${ex.message}"))

            if (cmd.exitCodeExceptionMapper != null) {
                cmd.exitCodeExceptionMapper.getExitCode(ex)
            } else {
                cmd.commandSpec.exitCodeOnExecutionException()
            }
        }

        return CommandLine(this, factory)
            .setExecutionExceptionHandler(errorHandler)
            .setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
