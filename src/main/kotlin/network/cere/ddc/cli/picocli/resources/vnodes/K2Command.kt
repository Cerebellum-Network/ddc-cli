package network.cere.ddc.cli.picocli.resources.vnodes

import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "k2")
class K2Command: AbstractCommand() {
    override fun run() {
        println("hello world!")
    }

}
