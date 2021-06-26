package boozilla.asset.module;

import boozilla.asset.protoc.Protobuf;
import com.google.inject.AbstractModule;

public class ProtobufModule extends AbstractModule {
    @Override
    protected void configure()
    {
        bind(Protobuf.class);
    }
}
