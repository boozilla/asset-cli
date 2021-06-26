package boozilla.asset.excel.type;

public class AInteger implements AType {
    public static final String ASSET_TYPE = "int";

    @Override
    public String getAssetType()
    {
        return ASSET_TYPE;
    }

    @Override
    public String getProtoType()
    {
        return "int32";
    }

    @Override
    public Object cast(final Object value)
    {
        return value == null ? 0 : Integer.parseInt((String) value);
    }
}
