package network.cere.ddc.cli.picocli

import com.fasterxml.jackson.annotation.JsonProperty
import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.`object`.ObjectStorage
import network.cere.ddc.`object`.ObjectStorageBuilder
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Producer
import network.cere.ddc.client.producer.ProducerConfig
import network.cere.ddc.core.signature.Scheme
import picocli.CommandLine
import kotlin.random.Random

abstract class AbstractCommand(private val ddcCliConfigFile: DdcCliConfigFile = DdcCliConfigFile()) : Runnable {

    @CommandLine.Option(
        names = ["-p", "--profile"],
        defaultValue = DdcCliConfigFile.DEFAULT_PROFILE,
        description = ["Configuration profile to use)"]
    )
    var profile: String? = null

    fun buildConsumer(configOptions: Map<String, String>): Consumer {
        val consumerConfig = ddcCliConfigFile.readConsumerConfig(configOptions)
        return DdcConsumer(consumerConfig, buildVertx())
    }

    fun buildProducer(producerConfig: ProducerConfig): Producer {
        return DdcProducer(producerConfig, buildVertx())
    }

    fun buildObjectStorage(configOptions: Map<String, String>): ObjectStorage {
        val trustedNodes = ddcCliConfigFile.readObjectStorageTrustedNodes(configOptions)
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = ddcCliConfigFile.readSignatureScheme(configOptions)

        return ObjectStorageBuilder().privateKey(privateKey).scheme(scheme).trustedNodes(trustedNodes).build()
    }

    fun buildContentAddressableStorage(configOptions: Map<String, String>): CereDdcStorage {
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = ddcCliConfigFile.readSignatureScheme(configOptions)
        val gatewayUrl = ddcCliConfigFile.readGatewayUrl(configOptions)

        return CereDdcStorage(gatewayUrl, privateKey, scheme)
    }

    fun buildKeyValueStorage(configOptions: Map<String, String>): CereDdcKeyValueStorage {
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = ddcCliConfigFile.readSignatureScheme(configOptions)
        val gatewayUrl = ddcCliConfigFile.readGatewayUrl(configOptions)

        return CereDdcKeyValueStorage(gatewayUrl, privateKey, scheme)
    }

    private fun buildVertx() = Vertx.vertx(
        VertxOptions().setFileSystemOptions(
            FileSystemOptions().setClassPathResolvingEnabled(false)
        )
    )

    //TODO Temporary, remove when client ready
    data class Tag(@field: JsonProperty("key") val key: String, @field: JsonProperty("value") val value: String)
    data class Piece(@field: JsonProperty("data") val data: ByteArray, @field: JsonProperty("tags") val tags: List<Tag>)
    data class Query(val tags: Map<String, String>)
    data class PieceUri(@field: JsonProperty("value") val value: String) {
        fun getBucketId(): String = "bucketId"
        fun getCid(): String = "cid"
    }
    class CereDdcStorage(gatewayUrl: String, privateKey: String, scheme: String = Scheme.SR_25519) {
        fun store(bucketId: Long, piece: Piece): PieceUri = PieceUri("http://www.something1.by")
        fun read(bucketId: Long, cid: String): Piece? = Piece(Random.Default.nextBytes(20), listOf(Tag("tag", "value")))
        fun delete(bucketId: Long, cid: String) {}
    }
    class CereDdcKeyValueStorage(gatewayUrl: String, privateKey: String, scheme: String = Scheme.SR_25519) {
        fun store(bucketId: Long, key: String, piece: Piece): PieceUri = PieceUri("http://www.something2.by")
        fun read(bucketId: Long, key: String): List<Piece> = listOf(Piece(Random.Default.nextBytes(10), listOf(Tag("tag1", "value1"))), Piece(Random.Default.nextBytes(20), listOf(Tag("tag2", "value2"))))
        fun delete(bucketId: Long, cid: String) {}
    }
}
