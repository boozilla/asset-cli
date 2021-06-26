package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class JavaScriptArgs {
    @CommandLine.Option(names = {"--js_out"}, description = "[JavaScript] Compile options", required = true)
    private String jsOut;

    public String getJsOut()
    {
        return "--js_out=" + jsOut;
    }
}
