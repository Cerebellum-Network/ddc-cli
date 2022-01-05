package network.cere.ddc.cli.picocli.keys

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.crypto.v1.toHex
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash.sha256
import kotlin.experimental.xor

private const val PBKDF2_ROUNDS = 2048
private const val SUBSTRATE_KEY_LENGTH = 32

private fun pbkdf2Seed(password: ByteArray, salt: ByteArray): ByteArray {
    val block = salt + byteArrayOf(0, 0, 0, 1)
    var prev = hmac(password, block)
    val md = prev.copyOf()
    repeat(PBKDF2_ROUNDS - 1) {
        prev = hmac(password, prev)
        prev.forEachIndexed { i, b ->
            md[i] = md[i] xor b
        }
    }
    return md.copyOfRange(0, SUBSTRATE_KEY_LENGTH)
}

private fun hmac(password: ByteArray, data: ByteArray): ByteArray {
    return HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_512, password)
        .apply { update(data) }
        .doFinal()
}

fun generateKeyPair(entropy: ByteArray, salt: ByteArray, scheme: String): KeyPairSeed {
    val secretSeed = pbkdf2Seed(entropy, salt)
    val secretSeedHex = secretSeed.toHex()

    when (scheme) {
        Scheme.SR_25519 -> {
            val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)
            return KeyPairSeed(
                keyPair.privateKey.toPrivateKey().toHex(),
                keyPair.publicKey.toPublicKey().toHex(),
                secretSeedHex
            )
        }
        Scheme.ED_25519 -> {
            val edDSANamedCurveSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
            val privKeySpec = EdDSAPrivateKeySpec(secretSeed, edDSANamedCurveSpec)
            val privKey = EdDSAPrivateKey(privKeySpec)

            return KeyPairSeed(
                privKey.geta().toHex(),
                privKey.abyte.toHex(),
                secretSeedHex
            )
        }
        Scheme.SECP_256_K_1 -> {
            val keyPair = ECKeyPair.create(sha256(secretSeed))

            return KeyPairSeed(
                keyPair.privateKey.toByteArray().toHex(),
                keyPair.publicKey.toByteArray().toHex(),
                secretSeedHex
            )
        }
        else -> {
            throw RuntimeException("Please provide a valid signature scheme: sr25519, ed25519, or secp256k1")
        }
    }
}