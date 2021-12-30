package network.cere.ddc.cli.config

import io.quarkus.runtime.annotations.RegisterForReflection
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.client.api.*
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
        SendPieceResponse::class,
        String::class,
        ObjectPath::class,
        Edek::class,
    ]
)
class ReflectionConfig