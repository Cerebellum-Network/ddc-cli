package network.cere.ddc.cli.picocli.resources.vnodes

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import org.json.JSONObject
import picocli.CommandLine

@CommandLine.Command(name = "recover")
class RecoverCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    companion object {
        const val CLUSTER_BASE_PATH = "/api/rest/clusters"
    }

    private val client: HttpClient = HttpClient()

    @CommandLine.Option(
        names = ["-c", "--clusterId"],
        required = true,
    )
    var clusterId: Long = -1

    @CommandLine.Option(
        names = ["-v", "--vnodeIndex"],
        required = true,
    )
    var vnodeIndex: Int = -1

    override fun run() {
        runCatching { runBlocking { runAsync() } }
            .onFailure { throw RuntimeException("Couldn't recover vnode", it) }
    }

    private suspend fun runAsync() {
        val smartContract = buildSmartContract(ddcCliConfigFile.read(profile))

        val cluster = smartContract.clusterGet(clusterId)

        if (vnodeIndex >= cluster.cluster.vnodes.size) {
            println("Invalid vnodeIndex (cluster has ${cluster.cluster.vnodes.size} vnodes")
            return
        }

        val nodeId = cluster.cluster.vnodes[vnodeIndex]
        println("Vnode assigned to node $nodeId")

        val node = smartContract.nodeGet(nodeId)

        val url = runCatching { JSONObject(node.params).getString("url") }
            .onFailure { println("Couldn't extract url from node params ${node.params}") }
            .getOrThrow()

        val response = client.post<HttpResponse>("$url$CLUSTER_BASE_PATH/$clusterId/vnodes/$vnodeIndex/recover")

        if (HttpStatusCode.OK != response.status) {
            println("Vnode recovery failed (statusCode=${response.status}, body=${String(response.receive<ByteArray>())}")
        } else {
            println("Vnode successfully recovered")
        }
    }
}
