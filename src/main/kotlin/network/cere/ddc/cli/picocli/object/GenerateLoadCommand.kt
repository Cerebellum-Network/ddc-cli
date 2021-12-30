package network.cere.ddc.cli.picocli.`object`

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

@CommandLine.Command(name = "generate-load")
class GenerateLoadCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--bucket-id"],
        description = ["Bucket Id where stored required object"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-u", "--users"],
        description = ["Number of concurrent users (default - 1)"]
    )
    var users: Int = 1

    @CommandLine.Option(
        names = ["-n", "--number"],
        description = ["Number of Object per user (default - int max value)"]
    )
    var number: Int = Int.MAX_VALUE

    @CommandLine.Option(
        names = ["-int", "--interval"],
        description = ["Interval between user requests (default - 1s)"]
    )
    var interval: Duration = Duration.ofSeconds(1)

    @CommandLine.Option(
        names = ["-s", "--size"],
        description = ["Object size to be generated in bytes (default - 1000)"]
    )
    var size: Int = 1000

    override fun run() {
        val configOptions = ddcCliConfigFile.read(profile)
        val storage = buildObjectStorage(configOptions)

        val generationThreads = mutableListOf<Thread>()
        repeat(users) {
            thread {
                repeat(number) {
                    val uniquePostfix = UUID.randomUUID().toString()
                    val data = "0".repeat(size - uniquePostfix.length) + uniquePostfix

                    runBlocking {
                        val objectPath = storage.storeObject(bucketId, data.toByteArray())

                        println("""{"url":"${objectPath.url}"}""")
                        delay(interval.toMillis())
                    }
                }
            }.also { generationThreads.add(it) }
        }
        generationThreads.forEach { it.join() }
    }
}
