package network.cere.ddc.cli.config

import io.quarkus.runtime.annotations.RegisterForReflection
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.PieceUri
import network.cere.ddc.storage.domain.Tag

@RegisterForReflection(
    targets = [
        Metadata::class,
        String::class,
        List::class,
        Piece::class,
        PieceUri::class,
        Tag::class
    ]
)
class ReflectionConfig