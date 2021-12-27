package network.cere.ddc.cli.config

import io.quarkus.runtime.annotations.RegisterForReflection
import network.cere.ddc.client.api.AppTopology
import network.cere.ddc.client.api.Metadata
import network.cere.ddc.client.api.NodeMetadata
import network.cere.ddc.client.api.Partition
import network.cere.ddc.client.api.PartitionTopology
import network.cere.ddc.client.producer.SendPieceResponse
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata

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
        NftPath::class,
        Edek::class,
        Erc1155Metadata::class,
        Erc721Metadata::class
    ]
)
class ReflectionConfig