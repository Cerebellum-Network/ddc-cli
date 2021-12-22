package network.cere.ddc.cli.picocli

import picocli.CommandLine
import io.quarkus.runtime.QuarkusApplication

import io.quarkus.runtime.annotations.QuarkusMain
import network.cere.ddc.cli.picocli.behaviour.*
import network.cere.ddc.cli.picocli.keys.ExtractKeysCommand
import network.cere.ddc.cli.picocli.keys.GenerateKeysCommand
import network.cere.ddc.cli.picocli.nft.NftCommand
import picocli.CommandLine.IFactory

@QuarkusMain
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        GenerateKeysCommand::class,
        ExtractKeysCommand::class,
        SignCommand::class,
        BenchmarkCommand::class,
        ConfigureCommand::class,
        ConsumeCommand::class,
        CreateAppCommand::class,
        GenerateLoadCommand::class,
        GetAppPiecesCommand::class,
        GetPieceCommand::class,
        GetUserPiecesCommand::class,
        ProduceCommand::class,
        NftCommand::class,
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
