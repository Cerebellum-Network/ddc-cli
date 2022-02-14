package network.cere.ddc.cli.picocli.kv

import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
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
        names = ["-d", "--data"],
        description = ["Data for storing"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val storage = buildKeyValueStorage(ddcCliConfigFile.read(profile))
        runCatching { storage.store(bucketId, key, Piece(Base64.getDecoder().decode(data), listOf())) }
            .onSuccess { println("Piece with key $key stored") }
            .onFailure { throw RuntimeException("Couldn't store piece with key $key in bucket $bucketId", it) }
    }
}