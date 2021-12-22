package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import kotlinx.coroutines.DelicateCoroutinesApi
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.cli.config.DdcCliConfigFile.Companion.APP_PRIV_KEY_CONFIG
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Producer
import network.cere.ddc.client.producer.ProducerConfig
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.client.HttpTransportClient
import picocli.CommandLine

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

    fun buildNftStorage(configOptions: Map<String, String>): NftStorage {
        val config = ddcCliConfigFile.readNftStorageConfig(configOptions)
        val privateKey = ddcCliConfigFile.readPrivateKey(configOptions)
        val scheme = Scheme.create(Scheme.SR_25519, privateKey)

        return NftStorage(HttpTransportClient(scheme, config))
    }

    private fun buildVertx() = Vertx.vertx(
        VertxOptions().setFileSystemOptions(
            FileSystemOptions().setClassPathResolvingEnabled(false)
        )
    )
}
