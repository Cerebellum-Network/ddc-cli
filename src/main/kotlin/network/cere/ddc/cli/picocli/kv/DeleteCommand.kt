package network.cere.ddc.cli.picocli.kv

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "delete")
class DeleteCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

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
        val storage = buildKeyValueStorage(ddcCliConfigFile.read(profile))
        runCatching { storage.delete(bucketId, cid) }
            .onSuccess { println("$cid in bucket $bucketId removed") }
            .onFailure { throw RuntimeException("Couldn't delete $cid in bucket $bucketId", it) }
    }
}