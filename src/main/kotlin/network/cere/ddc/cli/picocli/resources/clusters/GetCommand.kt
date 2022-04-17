package network.cere.ddc.cli.picocli.resources.clusters

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.response.ClusterStatus
import network.cere.ddc.contract.model.response.ResultList
import org.json.JSONObject
import picocli.CommandLine

@CommandLine.Command(name = "get")
class GetCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-c", "--clusterId"],
    )
    var clusterId: Long = -1

    @CommandLine.Option(
        names = ["-o", "--offset"],
    )
    var offset: Long = 0

    @CommandLine.Option(
        names = ["-l", "--limit"],
    )
    var limit: Long = 10

    @CommandLine.Option(
        names = ["-m", "--managerId"],
    )
    var managerId: String? = null

    override fun run() {
        runCatching { runBlocking { runAsync() } }
            .onFailure { throw RuntimeException("Couldn't get nodes", it) }
    }

    private suspend fun runAsync() {
        val smartContract = buildSmartContract(ddcCliConfigFile.read(profile))

        val clusterList = if (clusterId > -1L) {
            listOf(smartContract.clusterGet(clusterId))
        } else {
            smartContract.clusterList(offset, limit, managerId?.let { AccountId(it) })
        }

        if (clusterList.isEmpty()) {
            println("Not found")
            return
        }

        clusterList.forEach {
            println(
                "{clusterId: ${it.clusterId}, managerId: ${it.cluster.managerId}, " +
                        "vnodes: ${it.cluster.vnodes}, resourcePerVnode: ${it.cluster.resourcePerVnode}, " +
                        "resourceUsed: ${it.cluster.resourceUsed}}"
            )
        }
    }

}
