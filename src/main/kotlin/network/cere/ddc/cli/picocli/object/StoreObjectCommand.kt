package network.cere.ddc.cli.picocli.`object`

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "store-object")
class StoreObjectCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--bucket-id"],
        description = ["Bucket Id where store object"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Data to be stored in Object Storage"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val objectStorage = buildObjectStorage(ddcCliConfigFile.read(profile))
        val objectPath = runBlocking { objectStorage.storeObject(bucketId, data.toByteArray()) }

        println("Url: ${objectPath.url}")
    }
}
