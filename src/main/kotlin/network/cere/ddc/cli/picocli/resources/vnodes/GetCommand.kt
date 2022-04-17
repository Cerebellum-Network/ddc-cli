package network.cere.ddc.cli.picocli.resources.vnodes

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "get")
class GetCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-c", "--clusterId"],
        required = true,
    )
    var clusterId: Long = -1

    @CommandLine.Option(
        names = ["-n", "--nodeId"],
        required = true,
    )
    var nodeId: Long = -1

    override fun run() {
        runCatching { runBlocking { runAsync() } }
            .onFailure { throw RuntimeException("Couldn't get vNodes", it) }
    }

    private suspend fun runAsync() {
        val smartContract = buildSmartContract(ddcCliConfigFile.read(profile))
        val indexes = mutableListOf<Int>()
        smartContract.clusterGet(clusterId).cluster.vnodes.forEachIndexed { i, clusterNodeId ->
            if (nodeId == clusterNodeId) {
                indexes.add(i)
            }
        }

        if (indexes.isEmpty()) {
            println("Not found")
            return
        }

        indexes.forEach {
            println("{clusterId: $clusterId, index: $it, nodeId: $nodeId}")
        }
    }
}
