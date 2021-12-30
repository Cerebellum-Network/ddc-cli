package network.cere.ddc.cli.picocli.`object`

import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "read-object")
class ReadObjectCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-u", "--url"],
        description = ["Url where stored required object"],
        required = true
    )
    var url: String = ""

    override fun run() {
        val objectStorage = buildObjectStorage(ddcCliConfigFile.read(profile))
        val data = runBlocking { objectStorage.readObject(ObjectPath(url)) }

        println("Data: ${String(data)}")
    }
}
