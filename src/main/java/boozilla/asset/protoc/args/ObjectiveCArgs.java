package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class ObjectiveCArgs {
    @CommandLine.Option(names = {"--objc_out"}, description = "[Objective-C] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--objc_out=" + dist;
    }
}
