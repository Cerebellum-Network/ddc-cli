package network.cere.ddc.cli.picocli

import picocli.CommandLine
import java.util.Properties

class Version : CommandLine.IVersionProvider {
    private val versionProperties: Properties = Properties()

    init {
        versionProperties.load(this.javaClass.getResourceAsStream("/version.properties"))
    }

    override fun getVersion(): Array<String> {
        return arrayOf(versionProperties.getProperty("version"))
    }
}