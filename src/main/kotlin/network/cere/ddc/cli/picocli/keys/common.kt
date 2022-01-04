package network.cere.ddc.cli.picocli.keys

import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import network.cere.ddc.crypto.v1.toHex
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash.sha256
import kotlin.experimental.xor

private const val PBKDF2_ROUNDS = 2048
private const val SUBSTRATE_KEY_LENGTH = 32
const val SR_25519 = "sr25519"
const val ED_25519 = "ed25519"
const val SECP_256_K_1 = "secp256k1"

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

fun generateKeyPair(entropy: ByteArray, salt: ByteArray, scheme: String): network.cere.ddc.cli.picocli.keys.KeyPair {
    val secretSeed = pbkdf2Seed(entropy, salt)

    when (scheme) {
        SR_25519 -> {
            val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)
            return KeyPair(
                keyPair.privateKey.toPrivateKey().toHex(),
                keyPair.publicKey.toPublicKey().toHex()
            )
        }
        ED_25519 -> {
            val edDSANamedCurveSpec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
            val privKeySpec = EdDSAPrivateKeySpec(secretSeed, edDSANamedCurveSpec)
            val privKey = EdDSAPrivateKey(privKeySpec)

            return KeyPair(
                privKey.geta().toHex(),
                privKey.abyte.toHex()
            )
        }
        SECP_256_K_1 -> {
            val keyPair = ECKeyPair.create(sha256(secretSeed))

            return KeyPair(
                keyPair.privateKey.toByteArray().toHex(),
                keyPair.publicKey.toByteArray().toHex()
            )
        }
        else -> {
            throw RuntimeException("Please provide a valid signature scheme: sr25519, ed25519, or secp256k1")
        }
    }
}