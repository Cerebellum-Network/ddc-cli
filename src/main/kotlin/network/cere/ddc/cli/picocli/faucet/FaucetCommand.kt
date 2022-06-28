package network.cere.ddc.cli.picocli.faucet

import network.cere.ddc.cli.picocli.AbstractCommand
import org.eclipse.microprofile.rest.client.inject.RestClient
import picocli.CommandLine
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR

@CommandLine.Command(name = "faucet")
class FaucetCommand(@RestClient private val faucetApi: FaucetApi) : AbstractCommand() {

    companion object {
        private const val NETWORKS_DESCRIPTION = "Test networks: testnet, qanet or devnet"
    }

    private val SUPPORTED_NETWORKS = setOf("testnet", "qanet", "devnet")

    @CommandLine.Option(
        names = ["-n", "--network"],
        required = false,
        description = [NETWORKS_DESCRIPTION]
    )
    var network: String = "testnet"

    @CommandLine.Option(
        names = ["-a", "--address"],
        required = false,
        description = ["Address where to send tokens"]
    )
    lateinit var address: String

    override fun run() {
        if (!SUPPORTED_NETWORKS.contains(network.lowercase())) {
            throw RuntimeException("Unsupported test network. $NETWORKS_DESCRIPTION")
        }

        runCatching {
            val sendTokensResponse = faucetApi.sendTokens(SendTokensRequest(network, address))
            println(sendTokensResponse.msg)
        }.onFailure {
            if (it is WebApplicationException && it.response?.statusInfo?.family == CLIENT_ERROR) {
                println(it.response.readEntity(SendTokensResponse::class.java).msg)
            } else {
                throw it
            }
        }
    }
}
