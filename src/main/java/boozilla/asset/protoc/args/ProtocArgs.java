package boozilla.asset.protoc.args;

import picocli.CommandLine;

public class ProtocArgs {
    @CommandLine.ArgGroup(exclusive = false)
    private CppArgs cppArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private DartArgs dartArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private GoArgs goArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private JavaArgs javaArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private PythonArgs pythonArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private RubyArgs rubyArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private CsharpArgs csharpArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private ObjectiveCArgs objectiveCArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private JavaScriptArgs javaScriptArgs;

    @CommandLine.ArgGroup(exclusive = false)
    private PhpArgs phpArgs;

    public String getArgs()
    {
        if(cppArgs != null)
        {
            return cppArgs.getDist();
        }
        else if(dartArgs != null)
        {
            return dartArgs.getDist();
        }
        else if(goArgs != null)
        {
            return goArgs.getDist() + " " + goArgs.getOpt();
        }
        else if(javaArgs != null)
        {
            return javaArgs.getJavaDist() + (javaArgs.getKotlinDist() != null ? " " + javaArgs.getKotlinDist() : "");
        }
        else if(pythonArgs != null)
        {
            return pythonArgs.getDist();
        }
        else if(rubyArgs != null)
        {
            return rubyArgs.getDist();
        }
        else if(csharpArgs != null)
        {
            return csharpArgs.getDist() + (csharpArgs.getOpt() != null ? " " + csharpArgs.getOpt() : "");
        }
        else if(objectiveCArgs != null)
        {
            return objectiveCArgs.getDist();
        }
        else if(javaScriptArgs != null)
        {
            return javaScriptArgs.getJsOut();
        }
        else if(phpArgs != null)
        {
            return phpArgs.getDist();
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
