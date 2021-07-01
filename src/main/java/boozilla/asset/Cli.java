package boozilla.asset;

import picocli.CommandLine;

public class Cli {
    public static void main(String ... args)
    {
        final var cli = new CommandLine(new AssetCommands());

        if(args.length == 0)
        {
            cli.usage(System.out);
        }
        else
        {
            final var exitCode = new CommandLine(new AssetCommands()).execute(args);
            System.exit(exitCode);
        }
    }
}
