package boozilla.asset.module;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import picocli.CommandLine;

public class ModuleFactory implements CommandLine.IFactory {
    private final Injector injector = Guice.createInjector(
            new ProtobufModule()
    );

    @Override
    public <K> K create(Class<K> cls) throws Exception
    {
        try
        {
            return injector.getInstance(cls);
        }
        catch(ConfigurationException ex)
        {
            return CommandLine.defaultFactory().create(cls);
        }
    }
}
