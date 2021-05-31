package network.cere.ddc.cli.picocli

import com.google.crypto.tink.subtle.Ed25519Sign
import com.google.crypto.tink.subtle.Hex
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import picocli.CommandLine

@CommandLine.Command(name = "create-app")
class CreateAppCommand(
    private val ddcCliConfigFile: DdcCliConfigFile,
    vertx: Vertx
) : Runnable {

    private companion object {
        private const val TIER_ID = "2"
    }

    @CommandLine.Option(
        names = ["--appPubKey"],
        defaultValue = "1",
        description = ["Application public key"]
    )
    var appPubKey: String? = null

    @CommandLine.Option(
        names = ["--appPrivKey"],
        description = ["Application private key"]
    )
    var appPrivKey: String? = null

    @CommandLine.Option(
        names = ["--tierId"],
        defaultValue = TIER_ID,
        description = ["Id of the application tier)"]
    )
    var tierId: String? = null

    private val client = WebClient.create(vertx)

    override fun run() {
        if (appPubKey.isNullOrEmpty() || appPrivKey.isNullOrEmpty()) {
            val appKeyPair = Ed25519Sign.KeyPair.newKeyPair()
            appPubKey = Hex.encode(appKeyPair.publicKey)
            appPrivKey = Hex.encode(appKeyPair.privateKey)
        }
        val signer = Ed25519Sign(Hex.decode(appPrivKey))

        val createAppReq = mapOf(
            "appPubKey" to appPubKey,
            "tierId" to tierId,
            "signature" to Hex.encode(signer.sign("$appPubKey$TIER_ID".toByteArray()))
        ).let(::JsonObject)


        client.postAbs("${readBootstrapNodes()[0]}/api/rest/apps")
            .sendJsonObject(createAppReq)
            .onFailure().invoke { e -> println(e) }
            .onItem().invoke { res ->
                if (res.statusCode() != OK.code()) {
                    println(res.bodyAsString())
                }
            }
            .await().indefinitely()

        println("appPubKey: $appPubKey")
        println("appPrivKey: $appPrivKey")
    }

    private fun readBootstrapNodes(): List<String> {
        val configOptions = ddcCliConfigFile.read()

        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        return bootstrapNodesAsString.split(",")
    }

}
