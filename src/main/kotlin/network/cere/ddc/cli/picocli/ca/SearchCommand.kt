package network.cere.ddc.cli.picocli.ca

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.Tag
import picocli.CommandLine

@CommandLine.Command(name = "search")
class SearchCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-b", "--bucketId"],
        description = ["Bucket Id where stored piece"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-t", "--tag"],
        description = ["Tag for search"],
        required = true
    )
    var tags: Map<String, String> = mapOf()

    @CommandLine.Option(
        names = ["-r", "--readable"],
        description = ["Show piece data as string"]
    )
    var readable: Boolean = false

    override fun run() {
        val storage = buildContentAddressableStorage(ddcCliConfigFile.read(profile))
        val objectMapper = jacksonObjectMapper()

        runCatching { runBlocking { storage.search(Query(bucketId, tags.map { Tag(it.key, it.value) })) } }
            .onSuccess {
                println("Found Pieces:")
                it.pieces.forEach {
                    println(objectMapper.writeValueAsString(it))
                    if (readable) {
                        println("Readable data: '${String(it.data)}'")
                    }
                }
                println("=".repeat(10))
            }
            .onFailure { throw RuntimeException("Couldn't found pieces in bucket $bucketId with tags $tags", it) }
    }
}