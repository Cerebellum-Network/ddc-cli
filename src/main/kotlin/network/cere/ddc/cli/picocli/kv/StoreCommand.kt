package network.cere.ddc.cli.picocli.kv

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
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

    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Data for storing"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val storage = buildKeyValueStorage(ddcCliConfigFile.read(profile))

        runCatching {
            runBlocking {
                storage.store(
                    bucketId, key, Piece(Base64.getDecoder().decode(data), tags.map { Tag(it.key, it.value) })
                )
            }
        }
            .onSuccess { println("Piece with key $key stored. PieceUri: ${jacksonObjectMapper().writeValueAsString(it)}") }
            .onFailure { throw RuntimeException("Couldn't store piece with key $key in bucket $bucketId", it) }
    }
}