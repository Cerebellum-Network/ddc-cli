package network.cere.ddc.cli.picocli.faucet

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper
import javax.ws.rs.core.Response

class FaucetExceptionMapper: ResponseExceptionMapper<RuntimeException> {
    override fun toThrowable(response: Response?): RuntimeException? {
       if (response?.statusInfo?.family == Response.Status.Family.CLIENT_ERROR) {
            println(response.readEntity(SendTokensResponse::class.java).msg)
        }

        return null
    }
}
