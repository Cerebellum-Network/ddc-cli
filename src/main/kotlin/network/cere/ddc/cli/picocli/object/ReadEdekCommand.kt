package network.cere.ddc.cli.picocli.`object`

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "read-edek")
class ReadEdekCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-u", "--url"],
        description = ["Object url"],
        required = true
    )
    var url: String = ""

    @CommandLine.Option(
        names = ["-k", "--publicKey"],
        description = ["EDEK public key Hex"],
        required = true
    )
    var publicKey: String = ""

    override fun run() {
        val objectMapper = jacksonObjectMapper()
        val objectStorage = buildObjectStorage(ddcCliConfigFile.read(profile))
        val edek = runBlocking { objectStorage.readEdek(ObjectPath(url), publicKey) }

        println(objectMapper.writeValueAsString(edek))
    }
}
