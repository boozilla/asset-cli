package boozilla.asset.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AssetSchema {
    public static final int PRIMARY_COLUMN = 0;

    private final String packageName;
    private final List<AssetColumn> columns;
    private String sheetName;
    private int endOfCell;
    private int endOfRow;

    public AssetSchema(final String packageName)
    {
        this.packageName = packageName;
        this.columns = new ArrayList<>();
    }

    public void addColumn(final AssetColumn column)
    {
        columns.add(column);
    }

    public List<AssetColumn> columns()
    {
        return columns;
    }

    public String getSheetName()
    {
        return sheetName;
    }

    public void setSheetName(String sheetName)
    {
        this.sheetName = sheetName;
    }

    public int getEndOfCell()
    {
        return endOfCell;
    }

    public void setEndOfCell(int endOfCell)
    {
        this.endOfCell = endOfCell;
    }

    public int getEndOfRow()
    {
        return endOfRow;
    }

    public void setEndOfRow(int endOfRow)
    {
        this.endOfRow = endOfRow;
    }

    public boolean equalScope(final Scope scope)
    {
        return Scope.valueOf(columns.get(PRIMARY_COLUMN).getScope().toUpperCase()).in(scope);
    }

    public boolean isEmpty()
    {
        return columns.isEmpty();
    }

    public boolean nonEmpty()
    {
        return !isEmpty();
    }

    @Override
    public String toString()
    {
        final var sb = new StringBuilder();
        sb.append("syntax = \"proto3\";\r\n");
        sb.append("package "); sb.append(packageName); sb.append(";\r\n\r\n");
        sb.append("message "); sb.append(sheetName); sb.append(" {\r\n");

        if(!columns.isEmpty())
        {
            sb.append("\tmessage "); sb.append("Row"); sb.append(" {\r\n");

            IntStream.rangeClosed(1, columns.size()).forEach(idx -> {
                final var column = columns.get(idx - 1);
                sb.append("\t\t// "); sb.append(column.getDescription());  sb.append("\r\n");
                sb.append("\t\t");

                if(column.isArray())
                {
                    sb.append("repeated ");
                }

                sb.append(column.getType().getProtoType()); sb.append(" "); sb.append(column.getName()); sb.append(" = "); sb.append(idx); sb.append(";");
                sb.append(idx == columns.size() ? "\r\n" : "\r\n\n");
            });

            sb.append("\t}\r\n\n");
        }

        sb.append("\t"); sb.append("repeated Row row = 1;");  sb.append("\r\n");

        sb.append("}\r\n");

        return sb.toString();
    }
}
