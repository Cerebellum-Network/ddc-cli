package network.cere.ddc.cli.picocli.resources.nodes

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.contract.model.AccountId
import org.json.JSONObject
import picocli.CommandLine

@CommandLine.Command(name = "get")
class GetCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-n", "--nodeId"],
    )
    var nodeId: Long = -1

    @CommandLine.Option(
        names = ["-o", "--offset"],
    )
    var offset: Long = 0

    @CommandLine.Option(
        names = ["-l", "--limit"],
    )
    var limit: Long = 10

    @CommandLine.Option(
        names = ["--providerId"],
    )
    var providerId: String? = null

    override fun run() {
        println("runAsync")
        runCatching { runBlocking { runAsync() } }
            .onFailure { throw RuntimeException("Couldn't get nodes", it) }
        println("runAsyncFinished")
    }

    private suspend fun runAsync() {
        println("buildSmartContract")

        val smartContract = buildSmartContract(ddcCliConfigFile.read(profile))

        println("nodesGet")
        val nodeList = if (nodeId == -1L) {
            listOf(smartContract.nodeGet(nodeId))
        } else {
            smartContract.nodeList(offset, limit, providerId?.let { AccountId(it) })
        }

        println("nodesGot")
        if (nodeList.isEmpty()) {
            println("Not found")
        }

        nodeList.forEach {
            val url = runCatching { JSONObject(it.params).getString("url") }
                .getOrDefault { "unknown" }

            println(
                """
                        |{
                        |nodeId: ${it.nodeId}, 
                        |providerId: ${it.node.providerId},
                        |freeResource: ${it.node.freeResource}, 
                        |url: $url,
                        |}""".trimMargin()
            )
        }

        println("finalised")
    }
}
