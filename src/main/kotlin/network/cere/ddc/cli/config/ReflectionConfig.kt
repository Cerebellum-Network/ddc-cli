package network.cere.ddc.cli.config

import io.quarkus.runtime.annotations.RegisterForReflection
import network.cere.ddc.client.api.*
import network.cere.ddc.client.producer.SendPieceResponse
import network.cere.ddc.storage.domain.Piece

@RegisterForReflection(
    targets = [
        AppTopology::class,
        Metadata::class,
        PartitionTopology::class,
        Partition::class,
        NodeMetadata::class,
        network.cere.ddc.client.producer.Piece::class,
        network.cere.ddc.client.consumer.Piece::class,
        SendPieceResponse::class,
        String::class,
        Piece::class
    ]
)
class ReflectionConfig