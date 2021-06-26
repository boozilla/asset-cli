package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class DartArgs {
    @CommandLine.Option(names = {"--dart_out"}, description = "[DART] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--dart_out=" + dist;
    }
}
