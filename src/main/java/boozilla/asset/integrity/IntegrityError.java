package boozilla.asset.integrity;

public class IntegrityError {
    private final String message;
    private final String sheet;
    private final String column;
    private final String code;
    private final String value;
    private final String link;

    public IntegrityError(final String message, final String sheet, final String column, final String code, final String value, final String link)
    {
        this.message = message;
        this.sheet = sheet;
        this.column = column;
        this.code = code;
        this.value = value;
        this.link = link;
    }

    public String getMessage()
    {
        return this.message;
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
        return String.format("%s [sheet=%s, column=%s, code=%s, value=%s, link=%s]", getMessage(), getSheet(), getColumn(), getCode(), getValue(), getLink());
    }
}
