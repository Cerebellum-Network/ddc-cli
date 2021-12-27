package network.cere.ddc.cli.picocli.nft

import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.picocli.AbstractCommand
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Erc721Metadata
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
        names = ["-i", "--nft-id"],
        description = ["Nft Id where stored required asset"],
        required = true
    )
    var nftId: String = ""

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
        description = ["Min asset size to be generated in bytes (default - 4 KB)"]
    )
    var sizeMin: Int = 4 * 1 shl 10 // 4 KB

    @CommandLine.Option(
        names = ["-sMax", "--sizeMax"],
        description = ["Max asset size to be generated in bytes (default - 400 KB)"]
    )
    var sizeMax: Int = 400 * 1 shl 10 // 400 KB

    override fun run() {
        val storage = buildNftStorage(ddcCliConfigFile.read(profile))
        val allNft = write(storage)
        read(storage, allNft)
    }

    private fun write(storage: NftStorage): Map<Int, List<Nft>> {
        val totalNfts = AtomicInteger()
        val totalFailedNfts = AtomicInteger()
        val totalBytes = AtomicInteger()
        val endTime = System.currentTimeMillis() + Duration.ofSeconds(durationInSec).toMillis()

        val allNft = ConcurrentHashMap<Int, List<Nft>>()

        (0..users).map {
            thread {
                val storedNft = mutableListOf<Nft>()
                while (System.currentTimeMillis() < endTime) {
                    runCatching {
                        val fileName = UUID.randomUUID().toString()
                        val assetSize = Random.nextInt(sizeMin, sizeMax)
                        val asset = ("0".repeat(assetSize) + fileName).toByteArray()
                        val metadata = Erc721Metadata(
                            name = fileName,
                            description = "Benchmark test metadata",
                            image = "http://some-image-storage.com"
                        )
                        val edek = Edek(publicKey = fileName, value = "Benchmark test EDEK")

                        val nft = runBlocking {
                            val assetNftPath = storage.storeAsset(nftId, asset, fileName)
                            val metadataNftPath = storage.storeMetadata(nftId, metadata)
                            val storedEdek = storage.storeEdek(nftId, metadataNftPath, edek)
                            Nft(assetNftPath, metadataNftPath, storedEdek)
                        }

                        nft to asset.size
                    }.onSuccess {
                        totalNfts.incrementAndGet()
                        totalBytes.addAndGet(it.second)
                        storedNft.add(it.first)
                    }.onFailure {
                        totalFailedNfts.incrementAndGet()
                    }
                }
                allNft[it] = storedNft
            }
        }.forEach { it.join() }

        // print result
        println("=".repeat(60))
        println("Writing")
        println("Total success NFTs: ${totalNfts.get()}")
        println("Total failed NFTs: ${totalFailedNfts.get()}")
        println("Total success asset bytes: ${totalBytes.get()}")
        println("Created NFT/sec: ${totalNfts.get() / durationInSec.toDouble()}")
        println("Asset bytes/sec: ${totalBytes.get() / durationInSec.toDouble()}")

        return allNft.toMap()
    }

    private fun read(storage: NftStorage, allNft: Map<Int, List<Nft>>) {
        val totalAssetBytes = AtomicInteger()
        val totalNfts = AtomicInteger()
        val totalFailedNfts = AtomicInteger()

        val start = System.currentTimeMillis()

        (0..users).map {
            thread {
                allNft.getOrDefault(it, emptyList()).forEach {
                    runCatching {
                        runBlocking {
                            storage.readMetadata(nftId, it.metadataNftPath)
                            storage.readEdek(nftId, it.metadataNftPath, it.edek.publicKey!!)
                            storage.readAsset(nftId, it.assetNftPath)
                        }
                    }.onSuccess {
                        totalNfts.incrementAndGet()
                        totalAssetBytes.addAndGet(it.size)
                    }.onFailure {
                        totalFailedNfts.incrementAndGet()
                    }
                }
            }
        }.forEach { runBlocking { it.join() } }

        val durationInSec = (System.currentTimeMillis() - start) / 1000.0

        // print result
        println("=".repeat(60))
        println("Reading")
        println("Total success NFTs: ${totalNfts.get()}")
        println("Total failed NFTs: ${totalFailedNfts.get()}")
        println("Total success asset bytes: ${totalAssetBytes.get()}")
        println("Read NFT/sec: ${totalNfts.get() / durationInSec}")
        println("Asset Bytes/sec: ${totalAssetBytes.get() / durationInSec}")
    }

    private data class Nft(
        val assetNftPath: NftPath,
        val metadataNftPath: NftPath,
        val edek: Edek
    )
}
