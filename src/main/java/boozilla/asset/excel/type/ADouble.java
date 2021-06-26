package boozilla.asset.excel.type;

public class ADouble implements AType {
    public static final String ASSET_TYPE = "double";

    @Override
    public String getAssetType()
    {
        return ASSET_TYPE;
    }

    @Override
    public String getProtoType()
    {
        return "double";
    }

    @Override
    public Object cast(final Object value)
    {
        return value == null ? 0.0 : Double.parseDouble((String) value);
    }
}
