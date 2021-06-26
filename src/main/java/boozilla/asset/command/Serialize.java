package boozilla.asset.command;

import boozilla.asset.excel.Asset;
import boozilla.asset.excel.Scope;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "serialize", description = "Serialize data from excel files to protobuf binary")
public class Serialize implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Excel sheet file to be serialized")
    private File src;

    @CommandLine.Option(names = {"--struct"}, description = "Proto structure location", required = true)
    private String struct;

    @CommandLine.Option(names = {"--package"}, description = "Protobuf package name", required = true)
    private String packageName;

    @CommandLine.Option(names = {"--scope"}, description = "Specify data scope", required = true)
    private Scope scope;

    @CommandLine.Option(names = {"--aesKey"}, description = "AES encryption key")
    private String aesKey;

    @CommandLine.Option(names = {"--dist"}, description = "Binary data destination", required = true)
    private String dist;

    @Override
    public Integer call() throws Exception
    {
        final var fileList = new ArrayList<String>();

        if(src.isDirectory())
        {
            Files.newDirectoryStream(src.toPath()).forEach(file -> {
                if(file.toString().endsWith(".xlsx"))
                    fileList.add(file.toFile().getPath());
            });
        }
        else if(src.isFile())
        {
            fileList.add(src.getPath());
        }
        else
        {
            throw new RuntimeException("File does not exist");
        }

        for(final var filename : fileList)
        {
            final var asset = new Asset(filename, packageName, scope);
            asset.serialize(struct, dist, aesKey);
        }

        return CommandLine.ExitCode.OK;
    }
}
