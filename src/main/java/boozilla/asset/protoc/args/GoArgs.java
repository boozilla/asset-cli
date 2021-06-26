package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class GoArgs {
    @CommandLine.Option(names = {"--go_out"}, description = "[Go] Output destination", required = true)
    private String dist;

    @CommandLine.Option(names = {"--go_opt"}, description = "[Go] Golang options", required = true)
    private String opt;

    public String getDist()
    {
        return "--go_out=" + dist;
    }

    public String getOpt()
    {
        return "--go_opt=" + opt;
    }
}
