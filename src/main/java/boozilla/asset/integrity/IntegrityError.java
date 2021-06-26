package boozilla.asset.integrity;

public class IntegrityError {
    private final String sheet;
    private final String column;
    private final String code;
    private final String value;
    private final String link;

    public IntegrityError(final String sheet, final String column, final String code, final String value, final String link)
    {
        this.sheet = sheet;
        this.column = column;
        this.code = code;
        this.value = value;
        this.link = link;
    }

    public String getSheet()
    {
        return this.sheet;
    }

    public String getColumn()
    {
        return this.column;
    }

    public String getCode()
    {
        return this.code;
    }

    public String getValue()
    {
        return this.value;
    }

    public String getLink()
    {
        return this.link;
    }

    @Override
    public String toString()
    {
        return String.format("Integrity error [sheet=%s, column=%s, code=%s, value=%s, link=%s]", getSheet(), getColumn(), getCode(), getValue(), getLink());
    }
}
