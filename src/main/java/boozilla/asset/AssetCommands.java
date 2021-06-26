package boozilla.asset;

import boozilla.asset.command.GenerateProto;
import boozilla.asset.command.GenerateStruct;
import boozilla.asset.command.Integrity;
import boozilla.asset.command.Serialize;
import picocli.CommandLine;

@CommandLine.Command(name = "asset", subcommands = {
        GenerateProto.class,
        GenerateStruct.class,
        Serialize.class,
        Integrity.class
})
public class AssetCommands {
}
