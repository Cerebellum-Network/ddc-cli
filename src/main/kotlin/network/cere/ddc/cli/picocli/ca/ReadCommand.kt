package network.cere.ddc.cli.picocli.ca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

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

        runCatching { runBlocking { storage.read(bucketId, cid) } }
            .onSuccess { println(jacksonObjectMapper().writeValueAsString(it)) }
            .onFailure { throw RuntimeException("Couldn't read piece with cid $cid in bucket $bucketId", it) }
    }
}