package boozilla.asset.excel;

import boozilla.asset.excel.type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

public class AssetColumn {
    private static final Map<String, AType> availableTypes = new HashMap<>();

    static {
        final var boolType = new ABoolean();
        final var dateType = new ADate();
        final var doubleType = new ADouble();
        final var intType = new AInteger();
        final var longType = new ALong();
        final var stringType = new AString();

        availableTypes.put(boolType.getAssetType(), boolType);
        availableTypes.put(dateType.getAssetType(), dateType);
        availableTypes.put(doubleType.getAssetType(), doubleType);
        availableTypes.put(intType.getAssetType(), intType);
        availableTypes.put(longType.getAssetType(), longType);
        availableTypes.put(stringType.getAssetType(), stringType);
    }

    private String description;
    private String name;
    private AType type;
    private boolean isArray;
    private String scope;
    private boolean nullable;
    private String link;

    private final int columnIndex;

    public AssetColumn(final int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public AType getType()
    {
        return type;
    }

    public void setType(String typeName)
    {
        isArray = typeName.endsWith("[]");
        type = availableTypes.get(isArray ? typeName.replace("[]", "") : typeName);

        if(type == null)
            throw new RuntimeException(String.format("Unsupported asset type [desc = %s, name = %s, type = %s]", description, name, typeName));
    }

    public boolean isArray()
    {
        return isArray;
    }

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public boolean isNullable()
    {
        return nullable;
    }

    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
    }

    public String getLink()
    {
        return link.replaceAll("\\[.+]", "");
    }

    public boolean isLinkScope()
    {
        return Pattern.compile("\\[.+]").matcher(link).find();
    }

    public List<Long> getLinkScope()
    {
        final var scopeValues = new ArrayList<Long>();
        final var matcher = Pattern.compile("\\[.+]").matcher(link);

        if(matcher.find())
        {
            final var scopes = matcher.group(0).replace("[", "").replace("]", "").split(",");

            for(var scope : scopes)
            {
                scope = scope.trim();

                if(scope.contains("~"))
                {
                    final var split = scope.split("~");
                    LongStream.rangeClosed(Long.parseLong(split[0]), Long.parseLong(split[1])).forEach(scopeValues::add);
                }
                else
                {
                    scopeValues.add(Long.parseLong(scope));
                }
            }
        }

        return scopeValues;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public boolean isIgnore()
    {
        return getDescription() == null || getDescription().contentEquals("-");
    }

    public int getColumnIndex()
    {
        return columnIndex;
    }

    public boolean isPrimary()
    {
        return this.columnIndex == AssetSchema.PRIMARY_COLUMN + 1;
    }
}
