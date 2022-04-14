package network.cere.ddc.cli.picocli

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import network.cere.ddc.cli.picocli.ca.ContentAddressableCommand
import network.cere.ddc.cli.picocli.keys.ExtractKeysCommand
import network.cere.ddc.cli.picocli.keys.GenerateKeysCommand
import network.cere.ddc.cli.picocli.kv.KeyValueCommand
import network.cere.ddc.cli.picocli.resources.clusters.ClustersCommand
import network.cere.ddc.cli.picocli.resources.nodes.NodesCommand
import network.cere.ddc.cli.picocli.resources.vnodes.VNodesCommand
import picocli.CommandLine
import picocli.CommandLine.IFactory

@QuarkusMain
@CommandLine.Command(
    mixinStandardHelpOptions = true,
    subcommands = [
        // Common
        GenerateKeysCommand::class,
        ExtractKeysCommand::class,
        SignCommand::class,
        ConfigureCommand::class,
        // Storages
        ContentAddressableCommand::class,
        KeyValueCommand::class,
        // Smart contract resources management
        ClustersCommand::class,
        NodesCommand::class,
        VNodesCommand::class,
    ]
)
class DdcCommand(private val factory: IFactory) : QuarkusApplication {
    override fun run(vararg args: String): Int {
        return CommandLine(this, factory).setCaseInsensitiveEnumValuesAllowed(true).execute(*args)
    }
}
