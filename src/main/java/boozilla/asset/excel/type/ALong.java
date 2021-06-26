package boozilla.asset.excel.type;

public class ALong implements AType {
    public static final String ASSET_TYPE = "long";

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
        return value == null ? 0 : Long.parseLong((String) value);
    }
}
