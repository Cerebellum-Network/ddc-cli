package network.cere.ddc.cli.picocli.ca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.io.File

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

    @CommandLine.Option(
        names = ["-f", "--file"],
        paramLabel = "FILE",
        description = ["Path to file where save read bytes"],
        required = false
    )
    var file: File? = null

    @CommandLine.Option(
        names = ["-r", "--readable"],
        description = ["Tag for search"]
    )
    var readable: Boolean = false

    override fun run() {
        val storage = buildContentAddressableStorage(ddcCliConfigFile.read(profile))

        runCatching { runBlocking { storage.read(bucketId, cid) } }
            .onSuccess { piece ->
                file?.also {
                    it.createNewFile()
                    it.appendBytes(piece.data)
                } ?:  run {
                    println(jacksonObjectMapper().writeValueAsString(piece))
                    if (readable) {
                        println("Readable data: '${String(piece.data)}'")
                    }
                }
            }
            .onFailure { throw RuntimeException("Couldn't read piece with cid $cid in bucket $bucketId", it) }
    }
}