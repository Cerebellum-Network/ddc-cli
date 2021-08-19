package network.cere.ddc.cli.config

import io.quarkus.runtime.annotations.RegisterForReflection
import network.cere.ddc.client.api.AppTopology
import network.cere.ddc.client.api.Metadata
import network.cere.ddc.client.api.NodeMetadata
import network.cere.ddc.client.api.Partition
import network.cere.ddc.client.api.PartitionTopology
import network.cere.ddc.client.producer.SendPieceResponse

@RegisterForReflection(
    targets = [
        AppTopology::class,
        Metadata::class,
        PartitionTopology::class,
        Partition::class,
        NodeMetadata::class,
        network.cere.ddc.client.producer.Piece::class,
        network.cere.ddc.client.consumer.Piece::class,
        SendPieceResponse::class
    ]
)
class ReflectionConfig