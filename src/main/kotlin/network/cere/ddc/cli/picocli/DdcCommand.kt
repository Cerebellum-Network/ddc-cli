package network.cere.ddc.cli.picocli

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import network.cere.ddc.cli.picocli.`object`.ObjectStorageCommand
import network.cere.ddc.cli.picocli.ca.ContentAddressableCommand
import network.cere.ddc.cli.picocli.event.*
import network.cere.ddc.cli.picocli.keys.ExtractKeysCommand
import network.cere.ddc.cli.picocli.keys.GenerateKeysCommand
import network.cere.ddc.cli.picocli.kv.KeyValueCommand
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
        ObjectStorageCommand::class,
        ConsumeCommand::class,
        GetAppPiecesCommand::class,
        GetPieceCommand::class,
        GetUserPiecesCommand::class,
        ProduceCommand::class,
        EventStorageCommand::class,
        ContentAddressableCommand::class,
        KeyValueCommand::class
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
