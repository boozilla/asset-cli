package boozilla.asset.command;

import boozilla.asset.protoc.Protobuf;
import boozilla.asset.protoc.args.ProtocArgs;
import com.google.inject.Inject;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "gen-struct", description = "Convert the asset structure of the Excel files to the protobuf structure class.")
public class GenerateStruct implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Excel sheet file to be converted to protobuf structure class")
    private File src;

    @CommandLine.ArgGroup(exclusive = false)
    private ProtocArgs protocArgs;

    @Inject
    private Protobuf protobuf;

    @Override
    public Integer call() throws IOException, InterruptedException
    {
        final var args = protocArgs.getArgs();
        return protobuf.compile(src, args);
    }
}
