package boozilla.asset.command;

import boozilla.asset.excel.Asset;
import boozilla.asset.excel.Scope;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "gen-proto", description = "Convert asset data structure in excel sheets to proto format")
public class GenerateProto implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Excel sheet file to be converted to proto format")
    private File src;

    @CommandLine.Option(names = {"--package"}, description = "Protobuf package name", required = true)
    private String packageName;

    @CommandLine.Option(names = {"--scope"}, description = "Specify data scope", required = true)
    private Scope scope;

    @CommandLine.Option(names = {"--schema_dist"}, description = "Schema creation destination", required = true)
    private String dist;

    @Override
    public Integer call() throws Exception
    {
        final var fileList = new ArrayList<String>();

        if(src.isDirectory())
        {
            Files.newDirectoryStream(src.toPath()).forEach(file -> {
                if(file.getFileName().toString().endsWith(".xlsx"))
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
            asset.saveSchema(dist);
        }

        return CommandLine.ExitCode.OK;
    }
}
