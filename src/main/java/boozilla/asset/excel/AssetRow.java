package boozilla.asset.excel;

import boozilla.asset.excel.type.AType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class AssetRow {
    private final AssetColumn column;
    private final Map<Integer, Object> data;

    public AssetRow primary;

    public AssetRow(final AssetColumn column)
    {
        this.column = column;
        this.data = new HashMap<>();
    }

    public void setPrimary(final AssetRow primary)
    {
        this.primary = primary;
    }

    public Object getPrimary(final Integer rowNum)
    {
        return Objects.requireNonNullElse(this.primary, this).data.get(rowNum);
    }

    public String getName()
    {
        return this.column.getName();
    }

    public AType getType()
    {
        return column.getType();
    }

    public Scope getScope()
    {
        return Scope.valueOf(column.getScope().toUpperCase());
    }

    public void put(final Integer rowNum, final Object data)
    {
        this.data.put(rowNum, data);
    }

    public Object asValue(final int index)
    {
        return getType().cast(data.get(index));
    }

    public List<Object> asArray(final int index)
    {
        return getType().toArray(data.get(index));
    }

    public Stream<Object> stream()
    {
        return this.data.values().stream();
    }

    public Stream<Map.Entry<Integer, Object>> data()
    {
        return this.data.entrySet().stream();
    }
}
