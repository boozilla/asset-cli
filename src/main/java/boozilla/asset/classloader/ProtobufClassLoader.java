package boozilla.asset.classloader;

import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ProtobufClassLoader extends ClassLoader {
    private final String compileDist;

    public ProtobufClassLoader(final String classPath, final String packageName) throws IOException
    {
        this(classPath, packageName, "");
    }

    public ProtobufClassLoader(final String classPath, final String packageName, final String workspace) throws IOException
    {
        compileDist = workspace + "compile/";

        final var packagePath = packageName.replaceAll("\\.", "/");
        final var basePath = classPath + (classPath.endsWith("/") ? "" : "/") + packagePath;

        compile(basePath, packagePath);

        final var classFiles = findClassFiles(compileDist + packagePath);
        for(final var path : classFiles)
        {
            defineClass(packageName + "." + path.getFileName().toString().replace(".class", ""), Files.readAllBytes(path));
        }

        System.exit(0);
    }

    private void compile(final String basePath, final String packagePath) throws IOException
    {
        final var javaFiles = findJavaFiles(basePath);

        final var compiler = ToolProvider.getSystemJavaCompiler();
        javaFiles.forEach(path -> compiler.run(null, null, null, path.toString()));

        final var dist = new File(compileDist + packagePath);
        FileUtils.deleteDirectory(dist);

        for(final var compiled : FileUtils.listFiles(new File(basePath), new String[]{"class"}, true))
        {
            FileUtils.moveToDirectory(compiled, dist, true);
        }
    }

    private List<Path> findJavaFiles(final String path) throws IOException
    {
        return Files.list(Path.of(path)).filter(file -> file.toString().endsWith(".java")).toList();
    }

    private List<Path> findClassFiles(final String path) throws IOException
    {
        return Files.list(Path.of(path))
                .sorted((x, y) -> Integer.compare(y.getFileName().toString().length(), x.getFileName().toString().length()))
                .filter(file -> file.toString().endsWith(".class"))
                .toList();
    }

    private void defineClass(final String name, final byte[] classBytes)
    {
        defineClass(name, classBytes, 0, classBytes.length);
    }

    private String getProperties() throws IOException, InterruptedException
    {
        final var findEnvHome = List.of(Objects.requireNonNullElse(System.getenv("JAVA_HOME"), ""),
                    Objects.requireNonNullElse(System.getenv("GRAALVM_HOME"), ""))
                .stream()
                .filter(home -> home != null && !home.isEmpty())
                .findAny();

        final var envHome = findEnvHome.map(s -> s + "/bin/").orElse("");
        final var proc = Runtime.getRuntime().exec(envHome + "java -XshowSettings:properties -version");

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

        final var sb = new StringBuilder();
        try(
                final var isr = new InputStreamReader(proc.getErrorStream());
                final var rdr = new BufferedReader(isr);
        )
        {
            String line;
            while((line = rdr.readLine()) != null)
            {
                sb.append(line);
                sb.append("\n");
            }
        }

        final var ret = proc.waitFor();
        if(ret != CommandLine.ExitCode.OK)
        {
            throw new RuntimeException("Installed JDK path not found");
        }

        return sb.toString();
    }
}
