package boozilla.asset.classloader;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ProtobufClassLoader extends ClassLoader {
    public ProtobufClassLoader(final String classPath, final String packageName) throws IOException
    {
        final var basePath = classPath + (classPath.endsWith("/") ? "" : "/") + packageName.replaceAll("\\.", "/");

        compile(basePath);

        final var classFiles = findClassFiles(basePath);
        for(final var path : classFiles)
        {
            defineClass(packageName + "." + path.getFileName().toString().replace(".class", ""), Files.readAllBytes(path));
        }
    }

    private void compile(final String basePath) throws IOException
    {
        final var javaFiles = findJavaFiles(basePath);

        final var compiler = ToolProvider.getSystemJavaCompiler();
        javaFiles.forEach(path -> compiler.run(null, null, null, path.toString()));
    }

    private List<Path> findJavaFiles(final String path) throws IOException
    {
        return Files.list(Path.of(path)).filter(file -> file.toString().endsWith(".java")).collect(Collectors.toUnmodifiableList());
    }

    private List<Path> findClassFiles(final String path) throws IOException
    {
        return Files.list(Path.of(path))
                .sorted((x, y) -> Integer.compare(y.getFileName().toString().length(), x.getFileName().toString().length()))
                .filter(file -> file.toString().endsWith(".class"))
                .collect(Collectors.toUnmodifiableList());
    }

    private void defineClass(final String name, final byte[] classBytes)
    {
        defineClass(name, classBytes, 0, classBytes.length);
    }
}
