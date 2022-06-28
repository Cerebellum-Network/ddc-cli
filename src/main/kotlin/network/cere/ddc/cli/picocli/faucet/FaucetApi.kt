package network.cere.ddc.cli.picocli.faucet

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.POST

@RegisterRestClient(configKey="faucet-api")
interface FaucetApi {
    @POST
    fun sendTokens(sendTokensRequest: SendTokensRequest): SendTokensResponse
}
