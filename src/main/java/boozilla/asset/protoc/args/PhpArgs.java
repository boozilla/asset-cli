package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class PhpArgs {
    @CommandLine.Option(names = {"--php_out"}, description = "[PHP] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--php_out=" + dist;
    }
}
