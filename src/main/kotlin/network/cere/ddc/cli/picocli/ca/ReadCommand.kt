package network.cere.ddc.cli.picocli.ca

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "read")
class ReadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-b", "--bucketId"],
        description = ["Bucket Id where stored piece"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-c", "--cid"],
        description = ["Cid of stored piece"],
        required = true
    )
    var cid: String = ""

    override fun run() {
        val storage = buildContentAddressableStorage(ddcCliConfigFile.read(profile))

        val encoder = Base64.getEncoder()
        runCatching { storage.read(bucketId, cid) }
            .onSuccess { println("Piece with cid $cid: ${it?.let { encoder.encode(it.data) }}") }
            .onFailure { throw RuntimeException("Couldn't read piece with cid $cid in bucket $bucketId", it) }
    }
}