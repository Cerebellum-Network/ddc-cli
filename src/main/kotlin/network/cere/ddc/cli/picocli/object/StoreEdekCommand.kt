package network.cere.ddc.cli.picocli.`object`

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "store-edek")
class StoreEdekCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-u", "--url"],
        description = ["Object url"],
        required = true
    )
    var url: String = ""

    @CommandLine.Option(
        names = ["-k", "--publicKey"],
        description = ["Public key Hex for EDEK"],
        required = true
    )
    var publicKey: String = ""

    @CommandLine.Option(
        names = ["-v", "--value"],
        description = ["EDEK value"],
        required = true
    )
    var value: String = ""

    override fun run() {
        val objectMapper = jacksonObjectMapper()
        val objectStorage = buildObjectStorage(ddcCliConfigFile.read(profile))
        val storedEdek = runBlocking {
            objectStorage.storeEdek(ObjectPath(url), Edek(publicKey = publicKey, value = value))
        }

        println("Stored EDEK: ${objectMapper.writeValueAsString(storedEdek)}")
    }
}
