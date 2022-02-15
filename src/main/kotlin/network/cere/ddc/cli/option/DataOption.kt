package network.cere.ddc.cli.option

import picocli.CommandLine
import java.io.File

class DataOption {
    @CommandLine.Option(
        names = ["-d", "--data"],
        description = ["Data for piece"]
    )
    var data: String = ""

    @CommandLine.Option(
        names = ["-f", "--file"],
        paramLabel = "FILE",
        description = ["Path to file"]
    )
    var file: File? = null
}