package boozilla.asset.protoc;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Protobuf {
    private void checkAvailable() throws InterruptedException
    {
        try
        {
            exec("protoc", true);
        }
        catch(IOException e)
        {
            throw new RuntimeException("protoc command not found");
        }
    }

    public int compile(final File src, final String protocOptions) throws IOException, InterruptedException
    {
        if(!src.isDirectory())
        {
            throw new RuntimeException("Invalid proto directory path");
        }

        checkAvailable();

        final var outArgPattern = Pattern.compile("(--[a-z]+_out=)+");
        final var outArgs = Arrays.stream(protocOptions.split(" "))
                .filter(args -> outArgPattern.matcher(args).lookingAt())
                .toList();

        for(final var args : outArgs)
        {
            final var outPath = args.split("=")[1];
            FileUtils.forceMkdir(new File(outPath));
        }

        final var sb = new StringBuilder();
        sb.append("protoc --proto_path="); sb.append(src.getPath());
        sb.append(" ");
        sb.append(protocOptions);
        sb.append(" ");

        Files.newDirectoryStream(src.toPath()).forEach(file -> {
            if(Files.isDirectory(file))
                return;

            sb.append(file.getParent());
            sb.append("/");
            sb.append(file.getFileName().getFileName());
            sb.append(" ");
        });

        return exec(sb.toString());
    }

    private int exec(final String command) throws IOException, InterruptedException
    {
        return exec(command, false);
    }

    private int exec(final String command, final boolean silent) throws IOException, InterruptedException
    {
        final var proc = Runtime.getRuntime().exec(command);

        if(!silent)
        {
            try(
                    final var isr = new InputStreamReader(proc.getInputStream());
                    final var rdr = new BufferedReader(isr);
            )
            {
                String line;
                while((line = rdr.readLine()) != null)
                {
                    System.out.println(line);
                }
            }

            try(
                    final var isr = new InputStreamReader(proc.getErrorStream());
                    final var rdr = new BufferedReader(isr);
            )
            {
                String line;
                while((line = rdr.readLine()) != null)
                {
                    System.out.println(line);
                }
            }
        }

        return proc.waitFor();
    }
}
