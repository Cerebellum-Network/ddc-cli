package network.cere.ddc.cli.picocli

import io.vertx.core.VertxOptions
import io.vertx.core.file.FileSystemOptions
import io.vertx.mutiny.core.Vertx
import network.cere.ddc.cli.config.DdcCliConfigFile
import network.cere.ddc.client.consumer.Consumer
import network.cere.ddc.client.consumer.DdcConsumer
import network.cere.ddc.client.producer.DdcProducer
import network.cere.ddc.client.producer.Producer
import network.cere.ddc.client.producer.ProducerConfig
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

    private fun buildVertx() = Vertx.vertx(
        VertxOptions().setFileSystemOptions(
            FileSystemOptions().setClassPathResolvingEnabled(false)
        )
    )
}
