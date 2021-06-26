package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class PythonArgs {
    @CommandLine.Option(names = {"--python_out"}, description = "[Python] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--python_out=" + dist;
    }
}
