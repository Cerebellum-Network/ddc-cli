package network.cere.ddc.cli.picocli.resources.vnodes

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.api.StandardCommands
import io.emeraldpay.polkaj.apiws.PolkadotWsApi
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.quarkus.launcher.QuarkusLauncher.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.contract.blockchain.mapping.reader.MetadataReader
import network.cere.ddc.contract.blockchain.mapping.reader.skip.SkipReaderGenerator
import picocli.CommandLine
import java.nio.file.Files

@CommandLine.Command(name = "k1")
class K1Command(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile){
    val JACKSON = jacksonObjectMapper()
        .registerModule(PolkadotModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun run() {

        val options = ddcCliConfigFile.read(profile)
        println("initialising api")
        val api = PolkadotWsApi.newBuilder().objectMapper(JACKSON).connectTo(ddcCliConfigFile.readWsUrl(options)).build()
        println("api initialised")

        runBlocking {
            println("connecting api")

            val connected = api.connect().await()
            println("api connected")

            if (connected) {
                println("reading metadata")
                val metadata = api.execute(StandardCommands.getInstance().stateMetadata())
                    .await()
                    .let { ScaleCodecReader(it.bytes).read(MetadataReader) }
                println("metadata read")
                println(metadata.version)
            }


        }

        println("launch")
        runBlocking {
            launch { // launch a new coroutine and continue
                println("hello world async!") // print after delay
            }
        }
        println("stop")
    }

}
