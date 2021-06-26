package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class CsharpArgs {
    @CommandLine.Option(names = {"--csharp_out"}, description = "[C#] Output destination", required = true)
    private String dist;

    @CommandLine.Option(names = {"--csharp_opt"}, description = "[C#] C# options", required = true)
    private String opt;

    public String getDist()
    {
        return "--csharp_out=" + dist;
    }

    public String getOpt()
    {
        return "--csharp_opt='" + opt + "'";
    }
}
