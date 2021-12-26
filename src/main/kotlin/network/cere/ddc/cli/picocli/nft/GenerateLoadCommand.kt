package network.cere.ddc.cli.picocli.nft

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import picocli.CommandLine
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

@CommandLine.Command(name = "generate-load")
class GenerateLoadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["--nft-id"],
        description = ["Nft Id where stored required asset"],
        required = true
    )
    var nftId: String = ""

    @CommandLine.Option(
        names = ["-u", "--users"],
        description = ["Number of concurrent users (default - 1)"]
    )
    var users: Int = 1

    @CommandLine.Option(
        names = ["-n", "--number"],
        description = ["Number of pieces per user (default - int max value)"]
    )
    var number: Int = Int.MAX_VALUE

    @CommandLine.Option(
        names = ["-i", "--interval"],
        description = ["Interval between user requests (default - 1s)"]
    )
    var interval: Duration = Duration.ofSeconds(1)

    @CommandLine.Option(
        names = ["-s", "--size"],
        description = ["Piece size to be generated in bytes (default - 1000)"]
    )
    var size: Int = 1000

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val storage = buildNftStorage(configOptions)

        val generationThreads = mutableListOf<Thread>()
        repeat(users) {
            thread {
                repeat(number) {
                    val fileName = UUID.randomUUID().toString()
                    val data = "0".repeat(size - fileName.length) + fileName
                    val metadata = Erc721Metadata(
                        name = fileName,
                        description = "Generated metadata",
                        image = "http://some-image-storage.com"
                    )

                    runBlocking {
                        val assetNftPath = storage.storeAsset(nftId, data.toByteArray(), fileName)
                        val metadataNftPath = storage.storeMetadata(nftId, metadata)
                        storage.storeMetadata(nftId, metadata)

                        println("""{"assetUrl":"${assetNftPath.url}","metadataUrl":"${metadataNftPath.url}"}""")
                        delay(interval.toMillis())
                    }
                }
            }.also { generationThreads.add(it) }
        }
        generationThreads.forEach { it.join() }
    }
}
