package network.cere.ddc.cli.picocli.keys

import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import kotlin.experimental.xor

private const val PBKDF2_ROUNDS = 2048
private const val SUBSTRATE_KEY_LENGTH = 32

fun pbkdf2Seed(password: ByteArray, salt: ByteArray): ByteArray {
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
