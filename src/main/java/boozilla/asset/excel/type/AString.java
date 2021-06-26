package boozilla.asset.excel.type;

public class AString implements AType {
    public static final String ASSET_TYPE = "string";

    @Override
    public String getAssetType()
    {
        return ASSET_TYPE;
    }

    @Override
    public String getProtoType()
    {
        return "string";
    }

    @Override
    public Object cast(final Object value)
    {
        return value == null ? "" : value;
    }
}
