package network.cere.ddc.cli.picocli.ca

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
        names = ["-t", "--tag"],
        description = ["Tag for storing piece"],
        required = false
    )
    var tags: Map<String, String> = mapOf()

    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Data for piece"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val storage = buildContentAddressableStorage(ddcCliConfigFile.read(profile))

        runCatching {
            runBlocking {
                storage.store(bucketId, Piece(Base64.getDecoder().decode(data), tags.map { Tag(it.key, it.value) }))
            }
        }
            .onSuccess { println("Piece stored in bucket $bucketId") }
            .onFailure { throw RuntimeException("Couldn't store piece in bucket $bucketId", it) }
    }
}