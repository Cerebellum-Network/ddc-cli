package network.cere.ddc.cli.picocli.keys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CommonTest {
    @Test
    fun `Public key hex to SS58 address`() {
        //given
        val publicKey = "0xd049e851567f16d68523a645ee96465ceb678ad983fc08e6a38408ad10410c4d"
        val expectedSS58Address = "5GmomkEekQQ3BipMvjDCG5bXKvzwhUDdXEcQqXRWmdkNCYkL"

        //when
        val ss58Address = publicKeyToSS58Address(publicKey)

        //then
        assertEquals(expectedSS58Address, ss58Address)
    }
}
