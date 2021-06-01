package network.cere.ddc.cli.config

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import java.io.File
import kotlin.streams.asSequence

@DisabledOnOs(value = [OS.WINDOWS])
internal class DdcCliConfigFileTest {
    private val ddcCliConfigFilePath = System.getProperty("user.home") + "/.ddc/test-cli-config"

    private val testSubject = DdcCliConfigFile(ddcCliConfigFilePath)

    @BeforeEach
    fun cleanUp() {
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        ddcCliConfigFile.delete()
    }

    @Test
    fun `Returns empty config options when config file doesn't exist`() {
        //given

        //when
        val options = testSubject.read()

        //then
        assertTrue(options.isEmpty())
    }

    @Test
    fun `Returns valid config options when config file exists`() {
        //given
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        ddcCliConfigFile.parentFile.mkdirs()
        ddcCliConfigFile.createNewFile()
        ddcCliConfigFile.writeText(
            """
            [default]    
            appPubKey=some_app_pub_key_1
            appPrivKey=some_app_priv_key_1
            bootstrapNodes=http://node1
        """.trimIndent()
        )

        val expectedConfigOptions = mapOf(
            "appPubKey" to "some_app_pub_key_1",
            "appPrivKey" to "some_app_priv_key_1",
            "bootstrapNodes" to "http://node1"
        )

        //when
        val options = testSubject.read()

        //then
        assertEquals(expectedConfigOptions, options)
    }

    @Test
    fun `Write config options when file doesn't exists`() {
        val configOptions = mapOf(
            "appPubKey" to "some_app_pub_key_2",
            "appPrivKey" to "some_app_priv_key_2",
            "bootstrapNodes" to "http://node2"
        )

        //when
        testSubject.write(configOptions)

        //then
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        val config = ddcCliConfigFile.bufferedReader().lines().asSequence().joinToString("\n")

        assertEquals(
            "[default]\nappPubKey=some_app_pub_key_2\nappPrivKey=some_app_priv_key_2\nbootstrapNodes=http://node2",
            config
        )
    }

    @Test
    fun `Merges config options when file exists`() {
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        ddcCliConfigFile.parentFile.mkdirs()
        ddcCliConfigFile.createNewFile()
        ddcCliConfigFile.writeText(
            """
            [default]    
            appPubKey=some_app_pub_key_3
            appPrivKey=some_app_priv_key_3
            bootstrapNodes=http://node3
        """.trimIndent()
        )

        val configOptions = mapOf(
            "appPrivKey" to "some_app_priv_key_3_updated",
            "newOption" to "some_new_option"
        )

        //when
        testSubject.write(configOptions)

        //then
        val config = ddcCliConfigFile.bufferedReader().lines().asSequence().joinToString("\n")

        assertEquals(
            "[default]\n" +
                    "appPubKey=some_app_pub_key_3\n" +
                    "appPrivKey=some_app_priv_key_3_updated\n" +
                    "bootstrapNodes=http://node3\n" +
                    "newOption=some_new_option", config
        )
    }

    @Test
    fun `Write config options to new profile`() {
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        ddcCliConfigFile.parentFile.mkdirs()
        ddcCliConfigFile.createNewFile()
        ddcCliConfigFile.writeText(
            """
            [default]    
            appPubKey=some_app_pub_key_4
            appPrivKey=some_app_priv_key_4
            bootstrapNodes=http://node4
        """.trimIndent()
        )

        val configOptions = mapOf(
            "appPubKey" to "some_app_pub_key_5",
            "appPrivKey" to "some_app_priv_key_5",
            "bootstrapNodes" to "http://node5"
        )

        //when
        testSubject.write(configOptions, "new-profile")

        //then
        val config = ddcCliConfigFile.bufferedReader().lines().asSequence().joinToString("\n")

        assertEquals(
            "[default]\n" +
                    "appPubKey=some_app_pub_key_4\n" +
                    "appPrivKey=some_app_priv_key_4\n" +
                    "bootstrapNodes=http://node4\n" +
                    "[new-profile]\n" +
                    "appPubKey=some_app_pub_key_5\n" +
                    "appPrivKey=some_app_priv_key_5\n" +
                    "bootstrapNodes=http://node5", config
        )
    }

    @Test
    fun `Write config options to existsing profile`() {
        val ddcCliConfigFile = File(ddcCliConfigFilePath)
        ddcCliConfigFile.parentFile.mkdirs()
        ddcCliConfigFile.createNewFile()
        ddcCliConfigFile.writeText(
            """
            [default] 
            appPubKey=some_app_pub_key_6
            appPrivKey=some_app_priv_key_6
            bootstrapNodes=http://node6
            [existing-profile] 
            appPubKey=some_app_pub_key_7
            appPrivKey=some_app_priv_key_7
            bootstrapNodes=http://node7
        """.trimIndent()
        )

        val configOptions = mapOf(
            "appPrivKey" to "some_app_priv_key_7_updated",
            "newOption" to "some_new_option"
        )

        //when
        testSubject.write(configOptions, "existing-profile")

        //then
        val config = ddcCliConfigFile.bufferedReader().lines().asSequence().joinToString("\n")

        assertEquals(
            "[default]\n" +
                    "appPubKey=some_app_pub_key_6\n" +
                    "appPrivKey=some_app_priv_key_6\n" +
                    "bootstrapNodes=http://node6\n" +
                    "[existing-profile]\n" +
                    "appPubKey=some_app_pub_key_7\n" +
                    "appPrivKey=some_app_priv_key_7_updated\n" +
                    "bootstrapNodes=http://node7\n" +
                    "newOption=some_new_option", config
        )
    }
}