package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class CppArgs {
    @CommandLine.Option(names = {"--cpp_out"}, description = "[C++] Output destination", required = true)
    private String dist;

    public String getDist()
    {
        return "--cpp_out=" + dist;
    }
}
