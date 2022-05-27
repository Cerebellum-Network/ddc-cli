package network.cere.ddc.cli.picocli.kv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.option.DataOption
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Tag
import picocli.CommandLine
import java.util.*

@CommandLine.Command(name = "store")
class StoreCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-b", "--bucketId"],
        description = ["Bucket Id where store piece"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-k", "--key"],
        description = ["Key for storing piece"],
        required = true
    )
    var key: String = ""

    @CommandLine.Option(
        names = ["-t", "--tag"],
        description = ["Tag for storing piece"],
        required = false
    )
    var tags: Map<String, String> = mapOf()

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    lateinit var dataOption: DataOption

    override fun run() {
        val storage = buildKeyValueStorage(ddcCliConfigFile.read(profile))

        runCatching {
            val bytes = dataOption.file?.readBytes() ?: Base64.getDecoder().decode(dataOption.data)
            runBlocking {
                storage.store(bucketId, key, Piece(bytes, tags.map { Tag(it.key, it.value) }))
            }
        }
            .onSuccess { println("Piece with key $key stored. PieceUri: ${jacksonObjectMapper().writeValueAsString(it)}") }
            .onFailure {
                val message = it.message?.let { "Message: '$it'" } ?: ""
                throw RuntimeException("Couldn't store piece with key $key in bucket $bucketId. $message")
            }
    }
}