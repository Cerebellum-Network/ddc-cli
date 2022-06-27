package network.cere.ddc.cli.picocli.faucet

import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "faucet")
class FaucetCommand(private val faucetApi: FaucetApi) : AbstractCommand() {

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

        val sendTokensResponse = faucetApi.sendTokens(SendTokensRequest(network, address))

        println(sendTokensResponse.msg)
    }
}
