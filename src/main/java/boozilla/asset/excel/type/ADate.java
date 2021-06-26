package boozilla.asset.excel.type;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ADate implements AType {
    public static final String ASSET_TYPE = "date";

    @Override
    public String getAssetType()
    {
        return ASSET_TYPE;
    }

    @Override
    public String getProtoType()
    {
        return "int64";
    }

    @Override
    public Object cast(final Object value)
    {
        return value == null ? 0 : ZonedDateTime.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
                .toInstant()
                .toEpochMilli();
    }
}
