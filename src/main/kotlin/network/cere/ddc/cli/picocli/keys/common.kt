package network.cere.ddc.cli.picocli.keys

import cash.z.ecc.android.bip39.Mnemonics
import com.debuggor.schnorrkel.sign.ExpansionMode
import com.debuggor.schnorrkel.sign.KeyPair
import network.cere.ddc.core.signature.Ed25519
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.crypto.v1.toHex
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import kotlin.experimental.xor
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey

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

fun generateKeyPair(mnemonicWords: Mnemonics.MnemonicCode, saltPhrase: String, scheme: String): network.cere.ddc.cli.picocli.keys.KeyPair {
    when (scheme) {
        Scheme.SR_25519 -> {
            val secretSeed = pbkdf2Seed(mnemonicWords.toEntropy(), saltPhrase.toByteArray())
            val keyPair = KeyPair.fromSecretSeed(secretSeed, ExpansionMode.Ed25519)

            return KeyPair(
                keyPair.privateKey.toPrivateKey().toHex(),
                keyPair.publicKey.toPublicKey().toHex()
            )
        }
        Scheme.ED_25519 -> {
            val extendedKey = MnemonicWords(mnemonicWords.toList()).toKey("m", saltPhrase)
            val ed25519 = Ed25519(extendedKey.keyPair.privateKey.key.toByteArray().toHex())

            return KeyPair(
                extendedKey.keyPair.privateKey.key.toByteArray().toHex(),
                ed25519.publicKeyHex
            )
        }
        else -> {
            throw RuntimeException("Please provide a valid signature scheme: ${Scheme.SR_25519} or ${Scheme.ED_25519}")
        }
    }
}