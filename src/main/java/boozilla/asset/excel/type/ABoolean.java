package boozilla.asset.excel.type;

public class ABoolean implements AType {
    public static final String ASSET_TYPE = "boolean";

    @Override
    public String getAssetType()
    {
        return ASSET_TYPE;
    }

    @Override
    public String getProtoType()
    {
        return "bool";
    }

    @Override
    public Object cast(final Object value)
    {
        return value != null && Boolean.parseBoolean((String) value);
    }
}
