package boozilla.asset.command;

import boozilla.asset.integrity.DataIntegrityManager;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "integrity", description = "Asset sheet data integrity check")
public class Integrity implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Asset Excel file or directory path to check integrity")
    private File src;

    @CommandLine.Option(names = {"--package"}, description = "Protobuf package name", required = true)
    private String packageName;

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

        try(final var integrity = new DataIntegrityManager(packageName))
        {
            for(final var assetPath : fileList)
            {
                integrity.update(assetPath);
            }

            integrity.checkIntegrity().forEach(System.err::println);
        }

        return CommandLine.ExitCode.OK;
    }
}
