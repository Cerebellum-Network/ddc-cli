package network.cere.ddc.cli.picocli.`object`

import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.ObjectStorage
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random

@CommandLine.Command(name = "benchmark")
class BenchmarkCommand(private val ddcCliConfigFile: DdcCliConfigFile) : AbstractCommand(ddcCliConfigFile) {

    @CommandLine.Option(
        names = ["-i", "--bucket-id"],
        description = ["Bucket Id where stored required object"],
        required = true
    )
    var bucketId: Long = 0L

    @CommandLine.Option(
        names = ["-u", "--users"],
        description = ["Number of concurrent users (default - 10)"]
    )
    var users: Int = 10

    @CommandLine.Option(
        names = ["-d", "--duration"],
        description = ["Benchmark data generation duration in seconds (default - 1 min)"]
    )
    var durationInSec: Long = 60

    @CommandLine.Option(
        names = ["-sMin", "--sizeMin"],
        description = ["Min object size to be generated in bytes (default - 4 KB)"]
    )
    var sizeMin: Int = 4 * 1 shl 10 // 4 KB

    @CommandLine.Option(
        names = ["-sMax", "--sizeMax"],
        description = ["Max object size to be generated in bytes (default - 400 KB)"]
    )
    var sizeMax: Int = 400 * 1 shl 10 // 400 KB

    override fun run() {
        val storage = buildObjectStorage(ddcCliConfigFile.read(profile))
        val allContents = write(storage)
        read(storage, allContents)
    }

    private fun write(storage: ObjectStorage): Map<Int, List<Content>> {
        val totalContents = AtomicInteger()
        val totalFailedContents = AtomicInteger()
        val totalBytes = AtomicInteger()
        val endTime = System.currentTimeMillis() + Duration.ofSeconds(durationInSec).toMillis()

        val allContents = ConcurrentHashMap<Int, List<Content>>()

        (0..users).map {
            thread {
                val storedContents = mutableListOf<Content>()
                while (System.currentTimeMillis() < endTime) {
                    runCatching {
                        val objectPostfix = UUID.randomUUID().toString()
                        val objectSize = Random.nextInt(sizeMin, sizeMax)
                        val objectBytes = ("0".repeat(objectSize) + objectPostfix).toByteArray()
                        val edek = Edek(publicKey = objectPostfix, value = "Benchmark test EDEK")

                        val storedContent = runBlocking {
                            val objectPath = storage.storeObject(bucketId, objectBytes)
                            val storedEdek = storage.storeEdek(objectPath, edek)
                            Content(objectPath, storedEdek)
                        }

                        storedContent to objectBytes.size
                    }.onSuccess {
                        totalContents.incrementAndGet()
                        totalBytes.addAndGet(it.second)
                        storedContents.add(it.first)
                    }.onFailure {
                        totalFailedContents.incrementAndGet()
                    }
                }
                allContents[it] = storedContents
            }
        }.forEach { it.join() }

        // print result
        println("=".repeat(60))
        println("Writing")
        println("Total success contents: ${totalContents.get()}")
        println("Total failed contents: ${totalFailedContents.get()}")
        println("Total success object bytes: ${totalBytes.get()}")
        println("Created content/sec: ${totalContents.get() / durationInSec.toDouble()}")
        println("Object bytes/sec: ${totalBytes.get() / durationInSec.toDouble()}")

        return allContents.toMap()
    }

    private fun read(storage: ObjectStorage, allContents: Map<Int, List<Content>>) {
        val totalObjectBytes = AtomicInteger()
        val totalContents = AtomicInteger()
        val totalFailedContents = AtomicInteger()

        val start = System.currentTimeMillis()

        (0..users).map {
            thread {
                allContents.getOrDefault(it, emptyList()).forEach {
                    runCatching {
                        runBlocking {
                            storage.readEdek(it.objectPath, it.edek.publicKey!!)
                            storage.readObject(it.objectPath)
                        }
                    }.onSuccess {
                        totalContents.incrementAndGet()
                        totalObjectBytes.addAndGet(it.size)
                    }.onFailure {
                        totalFailedContents.incrementAndGet()
                    }
                }
            }
        }.forEach { runBlocking { it.join() } }

        val durationInSec = (System.currentTimeMillis() - start) / 1000.0

        // print result
        println("=".repeat(60))
        println("Reading")
        println("Total success contents: ${totalContents.get()}")
        println("Total failed contents: ${totalFailedContents.get()}")
        println("Total success object bytes: ${totalObjectBytes.get()}")
        println("Read content/sec: ${totalContents.get() / durationInSec}")
        println("Object Bytes/sec: ${totalObjectBytes.get() / durationInSec}")
    }

    private data class Content(
        val objectPath: ObjectPath,
        val edek: Edek
    )
}
