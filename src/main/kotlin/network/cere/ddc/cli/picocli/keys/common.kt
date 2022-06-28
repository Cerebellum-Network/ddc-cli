package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics
import io.ipfs.multibase.Base58
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.crypto.v1.hexToBytes
import network.cere.ddc.crypto.v1.toHex
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.bouncycastle.jcajce.provider.digest.Blake2b
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

fun generateKeyPair(mnemonicWords: Mnemonics.MnemonicCode, saltPhrase: String, scheme: String): KeyPair {
    val seed = runCatching { pbkdf2Seed(mnemonicWords.toEntropy(), saltPhrase.toByteArray()).toHex() }
        .getOrElse { throw RuntimeException(it.message?.removePrefix("Error: ")) }
    val publicKey = Scheme.create(scheme, seed).publicKeyHex

    return KeyPair(secretSeed = seed, publicKey = publicKey)
}

fun publicKeyToSS58Address(publicKey: String): String {
    val publicKeyBytes = publicKey.hexToBytes()

    // Based on polkadot ss58: https://github.com/polkadot-js/ss58/blob/master/index.js#L32
    val bytes = byteArrayOf(42) + publicKeyBytes

    val blake2b = Blake2b.Blake2b512()
    // https://github.com/polkadot-js/common/blob/master/packages/util-crypto/src/address/sshash.ts
    val hash = blake2b.digest("SS58PRE".toByteArray() + bytes)

    val complete = bytes + hash[0] + hash[1]

    return Base58.encode(complete);
}
