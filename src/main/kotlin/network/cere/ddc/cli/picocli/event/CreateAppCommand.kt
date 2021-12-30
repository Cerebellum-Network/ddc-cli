package network.cere.ddc.cli.picocli.event

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import com.debuggor.schnorrkel.sign.SigningContext
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.json.JsonObject
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.BOOTSTRAP_NODES_CONFIG
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.crypto.v1.hexToBytes
import network.cere.ddc.crypto.v1.toHex
import picocli.CommandLine

@CommandLine.Command(name = "create-app")
class CreateAppCommand(
    private val ddcCliConfigFile: DdcCliConfigFile,
    vertx: Vertx
) : AbstractCommand(ddcCliConfigFile) {

    private val client = WebClient.create(vertx)

    private val signingContext = SigningContext.createSigningContext("substrate".toByteArray())

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val appPubKey = configOptions[DdcCliConfigFile.APP_PUB_KEY_CONFIG]
        val appPrivKey = configOptions[DdcCliConfigFile.APP_PRIV_KEY_CONFIG]
        if (appPubKey.isNullOrEmpty() || appPrivKey.isNullOrEmpty()) {
            throw RuntimeException("Missing required parameter (appPubKey or appPrivKey). Please use 'configure' command.")
        }

        val keyPair = KeyPair.fromSecretSeed(appPrivKey.hexToBytes(), ExpansionMode.Ed25519)
        val signature = keyPair.sign(signingContext.bytes(appPubKey.toByteArray()))

        val createAppReq = mapOf(
            "appPubKey" to appPubKey,
            "signature" to signature.to_bytes().toHex()
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
    }

    private fun readBootstrapNodes(): List<String> {
        val configOptions = ddcCliConfigFile.read(profile)

        val bootstrapNodesAsString = configOptions[BOOTSTRAP_NODES_CONFIG]
        if (bootstrapNodesAsString.isNullOrEmpty()) {
            throw RuntimeException("Missing required parameter bootstrapNodes. Please use 'configure' command.")
        }

        return bootstrapNodesAsString.split(",")
    }
}
