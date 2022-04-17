package network.cere.ddc.cli.picocli.resources.clusters

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "replace")
class ReplaceCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-c", "--clusterId"],
        required = true
    )
    var clusterId: Long = -1

    @CommandLine.Option(
        names = ["-v", "--vnodeIndex"],
        required = true
    )
    var vnodeIndex: Long = -1

    @CommandLine.Option(
        names = ["-n", "--nodeId"],
        required = true
    )
    var nodeId: Long = -1

    override fun run() {
        runCatching { runBlocking { runAsync() } }
            .onFailure { throw RuntimeException("Couldn't replace cluster node", it) }
    }

    private suspend fun runAsync() {
        val smartContract = buildSmartContract(ddcCliConfigFile.read(profile))

        smartContract.clusterReplaceNode(clusterId, vnodeIndex, nodeId)

        val cluster = smartContract.clusterGet(clusterId)

        println(
            "{clusterId: ${cluster.clusterId}, managerId: ${cluster.cluster.managerId}, " +
                    "vnodes: ${cluster.cluster.vnodes}, resourcePerVnode: ${cluster.cluster.resourcePerVnode}, " +
                    "resourceUsed: ${cluster.cluster.resourceUsed}}"
        )
    }
}
