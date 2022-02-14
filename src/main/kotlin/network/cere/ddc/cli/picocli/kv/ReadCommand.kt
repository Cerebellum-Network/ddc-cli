package network.cere.ddc.cli.picocli.kv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

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
        val objectMapper = jacksonObjectMapper()

        runCatching { runBlocking { storage.read(bucketId, key) } }
            .onSuccess { pieces ->
                println("Pieces with key $key:")
                pieces.forEach { println(objectMapper.writeValueAsString(it)) }
                println("=".repeat(10))
            }
            .onFailure { throw RuntimeException("Couldn't read pieces with key $key in bucket $bucketId", it) }
    }
}