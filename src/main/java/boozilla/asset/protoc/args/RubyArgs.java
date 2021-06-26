package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class RubyArgs {
    @CommandLine.Option(names = {"--ruby_out"}, description = "[Ruby] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--ruby_out=" + dist;
    }
}
