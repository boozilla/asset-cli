package boozilla.asset.integrity;

import boozilla.asset.excel.Asset;
import boozilla.asset.excel.AssetColumn;
import boozilla.asset.excel.AssetSchema;
import boozilla.asset.excel.Scope;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataIntegrityManager implements AutoCloseable {
    private static final int QUERY_TIMEOUT = 60;

    private static final String CREATE_CODE_TABLE = """
           CREATE TABLE IF NOT EXISTS `%s` (
           		`code` VARCHAR NOT NULL,
           		PRIMARY KEY (`code`)
           );
            """;
    private static final String CREATE_LINK_SCOPE_TABLE = """
           CREATE TABLE IF NOT EXISTS `%s` (
            	`column` VARCHAR NOT NULL,
            	`code` VARCHAR NOT NULL
           );
            """;
    private static final String CREATE_LINK_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s` (
                `sheet` VARCHAR NOT NULL,
            	`column` VARCHAR NOT NULL,
            	`code` VARCHAR NOT NULL,
            	`value` VARCHAR NOT NULL,
            	`link` VARCHAR NOT NULL,
            	`scope` BOOLEAN NOT NULL
            );
            """;
    private static final String CREATE_LINK_TABLE_INDICES = """
            CREATE INDEX idx_%s_Link ON %s (`link`);
            CREATE INDEX idx_%s_Scope ON %s (`scope`);
            """;
    private static final String CREATE_LINK_SCOPE_TABLE_INDICES = """
            CREATE INDEX idx_%s_LinkScope ON %s (`column`, `code`);
            """;
    private static final String DROP_TABLE = """
            DROP TABLE IF EXISTS %s;
            """;
    private static final String INSERT = """
            INSERT INTO %s VALUES %s;
            """;
    private static final String FIND_TABLE_NAMES = """
            SELECT
                name
            FROM
                sqlite_master
            WHERE
                type IN ('table' , 'view')
                    AND name NOT LIKE 'sqlite_%'
            UNION ALL SELECT
                name
            FROM
                sqlite_temp_master
            WHERE
                type IN ('table' , 'view')
            ORDER BY 1;
            """;
    private static final String FIND_LINK_TABLE = """
            SELECT link FROM %s GROUP BY link
            """;
    private static final String FIND_LINK_ERROR = """
            SELECT
                l.*, c.code IS NULL AS error
            FROM
                %s l
                    LEFT JOIN
                %s_Code c ON c.code = l.value
            WHERE
                l.link = '%s';
            """;
    private static final String FIND_LINK_SCOPE_ERROR = """
            SELECT
                l.*, ls.code IS NULL AS error
            FROM
                %s l
                    LEFT JOIN
                %sScope ls ON l.column = ls.column AND l.value = ls.code
            WHERE
                l.scope = 'true'
            """;

    private final Connection connection;
    private final String packageName;

    public DataIntegrityManager(final String packageName) throws SQLException
    {
        this.connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", packageName.replaceAll("\\.", "-")));
        this.packageName = packageName;
    }

    public void update(final String filename) throws SQLException, IOException
    {
        final var asset = new Asset(filename, packageName, Scope.SHARE);

        try(final var statement = connection.createStatement())
        {
            statement.setQueryTimeout(QUERY_TIMEOUT);

            for(final var schema : asset.getSchemas())
            {
                createTable(schema.getSheetName() + "_Code", CREATE_CODE_TABLE, statement, true);
                createTable(schema.getSheetName() + "_LinkScope", CREATE_LINK_SCOPE_TABLE, statement, true);
                createTable(schema.getSheetName() + "_Link", CREATE_LINK_TABLE, statement, true);
                statement.executeUpdate(String.format(CREATE_LINK_TABLE_INDICES, schema.getSheetName(), schema.getSheetName() + "_Link", schema.getSheetName(), schema.getSheetName() + "_Link"));
                statement.executeUpdate(String.format(CREATE_LINK_SCOPE_TABLE_INDICES, schema.getSheetName(), schema.getSheetName() + "_LinkScope"));

                final var rows = asset.getRows(schema);
                final var codeDML = rows.stream().filter(row -> row.getName().contentEquals(schema.columns().get(AssetSchema.PRIMARY_COLUMN).getName()))
                        .map(row -> String.format(INSERT, schema.getSheetName() + "_Code", row.stream().map(value -> "(" + value + ")").collect(Collectors.joining(", "))))
                        .findAny();

                if(codeDML.isPresent())
                    statement.executeUpdate(codeDML.get());

                final var linkedColumns = schema.columns().stream().filter(column -> !column.getLink().isEmpty()).collect(Collectors.toMap(AssetColumn::getName, column -> column));

                if(!linkedColumns.isEmpty())
                {
                    final var linkTables = new ArrayList<String>();
                    final var lnikedValues = rows.stream().filter(row -> linkedColumns.containsKey(row.getName()))
                            .map(row -> {
                                final var column = row.getName();
                                final var link = linkedColumns.get(column).getLink();
                                final var values = new ArrayList<Map.Entry<Integer, Object>>();

                                linkTables.add(link);

                                row.data().filter(it -> it.getValue() != null).forEach(it -> {
                                    if(linkedColumns.get(column).isArray())
                                    {
                                        final var entries = row.getType().toArray(it.getValue()).stream().map(val -> new AbstractMap.SimpleEntry<>(it.getKey(), val)).toList();
                                        values.addAll(entries);
                                    }
                                    else
                                    {
                                        values.add(new AbstractMap.SimpleEntry<>(it.getKey(), it.getValue()));
                                    }
                                });

                                return values.stream().map(entry -> "(" + String.join(",", "'" + schema.getSheetName() + "'", "'" + column + "'", "'" + row.getPrimary(entry.getKey()) + "'", "'" + entry.getValue() + "'", "'" + link + "'", "'" + linkedColumns.get(column).isLinkScope() + "'") + ")")
                                        .collect(Collectors.joining(", "));
                            }).collect(Collectors.joining(", "));

                    final var linkScopeValues = rows.stream()
                            .filter(row -> linkedColumns.containsKey(row.getName()))
                            .filter(row -> linkedColumns.get(row.getName()).isLinkScope())
                            .map(row -> linkedColumns.get(row.getName()).getLinkScope().stream().map(entry -> "(" + String.join(",", "'" + row.getName() + "'", "'" + entry + "'") + ")").collect(Collectors.joining(", ")))
                            .collect(Collectors.joining(", "));

                    for(final var linkTable : linkTables)
                    {
                        createTable(linkTable + "_Code", CREATE_CODE_TABLE, statement, false);
                    }

                    statement.executeUpdate(String.format(INSERT, schema.getSheetName() + "_Link", lnikedValues));
                    statement.executeUpdate(String.format(INSERT, schema.getSheetName() + "_LinkScope", linkScopeValues));
                }
            }
        }
    }

    private void createTable(final String tableName, final String ddlQuery, final Statement statement, final boolean createNew) throws SQLException
    {
        if(createNew)
            statement.executeUpdate(String.format(DROP_TABLE, tableName));

        final var ddl = String.format(ddlQuery, tableName);
        statement.executeUpdate(ddl);
    }

    public List<IntegrityError> checkIntegrity() throws SQLException
    {
        final var errors = new ArrayList<IntegrityError>();
        try(final var statement = connection.createStatement())
        {
            statement.setQueryTimeout(QUERY_TIMEOUT);

            final var findTables = statement.executeQuery(FIND_TABLE_NAMES);
            final var tables = new ArrayList<String>(findTables.getFetchSize());

            while(findTables.next())
            {
                tables.add(findTables.getString("name"));
            }

            final var targetTables = tables.stream().filter(table -> table.endsWith("_Link")).toList();

            for(final var targetTable : targetTables)
            {
                final var findLink = statement.executeQuery(String.format(FIND_LINK_TABLE, targetTable));
                final var links = new ArrayList<String>();
                while(findLink.next())
                {
                    links.add(findLink.getString("link"));
                }

                for(final var linkTable : links)
                {
                    final var findLinkError = statement.executeQuery(String.format(FIND_LINK_ERROR, targetTable, linkTable, linkTable));
                    while(findLinkError.next())
                    {
                        if(findLinkError.getBoolean("error"))
                        {
                            final var error = new IntegrityError("Linked code or table does not exist", findLinkError.getString("sheet"), findLinkError.getString("column"), findLinkError.getString("code"), findLinkError.getString("value"), findLinkError.getString("link"));
                            errors.add(error);
                        }
                    }

                    final var findLinkScopeError = statement.executeQuery(String.format(FIND_LINK_SCOPE_ERROR, targetTable, targetTable));
                    while(findLinkScopeError.next())
                    {
                        if(findLinkScopeError.getBoolean("error"))
                        {
                            final var error = new IntegrityError("Out of range of specified Link code", findLinkScopeError.getString("sheet"), findLinkScopeError.getString("column"), findLinkScopeError.getString("code"), findLinkScopeError.getString("value"), findLinkScopeError.getString("link"));
                            errors.add(error);
                        }
                    }
                }
            }
        }

        return errors;
    }

    @Override
    public void close() throws Exception
    {
        connection.close();
    }
}
