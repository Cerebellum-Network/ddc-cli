package network.cere.ddc.cli.config

data class EncryptionConfig(
    val masterEncryptionKey: String,
    val encryptionJsonPaths: List<String>
)