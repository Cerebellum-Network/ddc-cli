package network.cere.ddc.cli.picocli.kv

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "read")
class ReadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-b", "--bucketId"],
        description = ["Bucket Id where stored pieces"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-k", "--key"],
        description = ["Required key of stored pieces"],
        required = true
    )
    var key: String = ""

    override fun run() {
        val storage = buildKeyValueStorage(ddcCliConfigFile.read(profile))
        val encoder = Base64.getEncoder()
        runCatching { storage.read(bucketId, key) }
            .onSuccess { pieces ->
                println("Pieces with key $key:")
                pieces.forEach { println(encoder.encode(it.data)) }
                println("=".repeat(10))
            }
            .onFailure { throw RuntimeException("Couldn't read pieces with key $key in bucket $bucketId", it) }
    }
}