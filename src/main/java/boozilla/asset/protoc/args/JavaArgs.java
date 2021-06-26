package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class JavaArgs {
    @CommandLine.Option(names = {"--java_out"}, description = "[Java] Output destination", required = true)
    private String javaDist;

    @CommandLine.Option(names = {"--kotlin_out"}, description = "[Kotlin] Output destination")
    private String kotlinDist;

    public String getJavaDist()
    {
        return "--java_out=" + javaDist;
    }

    public String getKotlinDist()
    {
        return kotlinDist != null ? "--kotlin_out=" + kotlinDist : null;
    }
}
