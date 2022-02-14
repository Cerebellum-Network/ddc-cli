package network.cere.ddc.cli.picocli.ca

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
        names = ["-d", "--data"],
        description = ["Data for piece"],
        required = true
    )
    var data: String = ""

    override fun run() {
        val storage = buildContentAddressableStorage(ddcCliConfigFile.read(profile))

        runCatching { storage.store(bucketId, Piece(Base64.getDecoder().decode(data), listOf())) }
            .onSuccess { println("Piece stored in bucket $bucketId") }
            .onFailure { throw RuntimeException("Couldn't store piece in bucket $bucketId", it) }
    }
}