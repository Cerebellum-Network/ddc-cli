package network.cere.ddc.cli.picocli.resources.vnodes

import io.quarkus.launcher.QuarkusLauncher.launch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.cere.ddc.cli.picocli.AbstractCommand
import picocli.CommandLine

@CommandLine.Command(name = "k1")
class K1Command : AbstractCommand() {
    override fun run() {
        println("launch")
        runBlocking {
            launch { // launch a new coroutine and continue
                println("hello world async!") // print after delay
            }
        }
        println("stop")
    }

}
