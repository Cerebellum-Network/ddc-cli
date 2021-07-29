package network.cere.ddc.cli.picocli

import com.google.crypto.tink.subtle.Ed25519Sign
import com.google.crypto.tink.subtle.Hex
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.DEFAULT_PROFILE
import picocli.CommandLine

@CommandLine.Command(name = "create-app")
class CreateAppCommand(
    private val ddcCliConfigFile: DdcCliConfigFile,
    vertx: Vertx
) : Runnable {

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
        names = ["-p", "--profile"],
        defaultValue = DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    private val client = WebClient.create(vertx)

    override fun run() {
        if (appPubKey.isNullOrEmpty() || appPrivKey.isNullOrEmpty()) {
            val appKeyPair = Ed25519Sign.KeyPair.newKeyPair()
            appPubKey = Hex.encode(appKeyPair.publicKey)
            appPrivKey = Hex.encode(appKeyPair.privateKey)
        }
        val signer = Ed25519Sign(Hex.decode(appPrivKey!!.removePrefix("0x")).sliceArray(0 until 32))

        val createAppReq = mapOf(
            "appPubKey" to appPubKey,
            "signature" to Hex.encode(signer.sign("$appPubKey".toByteArray()))
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
        val configOptions = ddcCliConfigFile.read(profile)

        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString == null || bootstrapNodesAsString.isEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        return bootstrapNodesAsString.split(",")
    }

}
