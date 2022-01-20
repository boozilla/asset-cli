package boozilla.asset.excel;

import boozilla.asset.classloader.ProtobufClassLoader;
import boozilla.asset.crypto.AESCrypt;
import boozilla.asset.excel.type.ADouble;
import boozilla.asset.excel.type.AInteger;
import boozilla.asset.excel.type.ALong;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Asset {
    static class MutableObject<T> {
        private T obj;

        public void set(T obj)
        {
            this.obj = obj;
        }

        public T get()
        {
            return this.obj;
        }
    }

    private static final AbstractMap.SimpleEntry<Integer, Integer> COLUMN_ROW_RANGE = new AbstractMap.SimpleEntry<>(1, 6);
    private static final int DATA_ROW_START_AT = 7;
    private static final int COLUMN_CELL_START_AT = COLUMN_ROW_RANGE.getKey();

    private final List<Sheet> sheets;
    private final List<AssetSchema> schemas;
    private final Scope scope;

    private String packageName;

    public Asset(final ByteBuffer byteBuffer, final String packageName, final Scope scope) throws IOException
    {
        this(new ByteArrayInputStream(byteBuffer.array()), scope);

        this.packageName = packageName;
    }

    public Asset(final String filename, final String rootPackage, final Scope scope) throws IOException
    {
        this(new FileInputStream(filename), scope);

        final var pathSplit = filename.split("/");
        final var filenameSplit = pathSplit[pathSplit.length - 1].split("\\.");

        this.packageName = rootPackage + "." + filenameSplit[0].toLowerCase();
    }

    private Asset(final InputStream inputStream, final Scope scope) throws IOException
    {
        this.scope = scope;
        this.sheets = getSheets(getWorkbook(inputStream));
        this.schemas = getSchemas(sheets);
    }

    private Workbook getWorkbook(final InputStream inputStream) throws IOException
    {
        System.out.printf("Read asset data [length = %s]%n", inputStream.available());
        return WorkbookFactory.create(inputStream);
    }

    private List<Sheet> getSheets(final Workbook workbook)
    {
        return IntStream.range(0, workbook.getNumberOfSheets())
                .mapToObj(workbook::getSheetAt)
                .peek(sheet -> System.out.printf("Found asset sheet [name = %s]%n", sheet.getSheetName()))
                .toList();
    }

    private int findEndOfCell(final Sheet sheet)
    {
        final var endOfCell = new AtomicInteger(0);
        final var row = sheet.getRow(COLUMN_CELL_START_AT);

        for(var columnIndex = COLUMN_CELL_START_AT; columnIndex < Integer.MAX_VALUE; columnIndex++)
        {
            final var cell = row.getCell(columnIndex);
            if(cell.getStringCellValue().isBlank())
                break;

            endOfCell.getAndIncrement();
        }

        return endOfCell.get();
    }

    private int findEndOfRow(final Sheet sheet)
    {
        final var endOfRow = new AtomicInteger(DATA_ROW_START_AT);

        for(var rowIndex = DATA_ROW_START_AT; rowIndex < Integer.MAX_VALUE; rowIndex++)
        {
            final var row = sheet.getRow(rowIndex);

            if(row == null)
                break;

            final var cell = row.getCell(COLUMN_CELL_START_AT);

            if(cell == null || cell.getCellType() == CellType.BLANK)
                break;

            endOfRow.getAndIncrement();
        }

        return endOfRow.get();
    }

    private List<AssetSchema> getSchemas(final List<Sheet> sheets)
    {
        return sheets.stream().map(sheet -> {
            System.out.printf("Parsing schema [sheetName = %s, package = %s]%n", sheet.getSheetName(), packageName);

            final var schema = new AssetSchema(packageName);
            schema.setSheetName(sheet.getSheetName());
            schema.setEndOfCell(findEndOfCell(sheet));
            schema.setEndOfRow(findEndOfRow(sheet));

            IntStream.rangeClosed(COLUMN_CELL_START_AT, schema.getEndOfCell()).forEach(columnIndex -> {
                final var column = new AssetColumn(columnIndex);

                IntStream.rangeClosed(COLUMN_ROW_RANGE.getKey(), COLUMN_ROW_RANGE.getValue()).forEach(rowIndex -> {
                    if (rowIndex > 1 && column.isIgnore()) {
                        return;
                    }

                    final var row = sheet.getRow(rowIndex);
                    final var cell = row.getCell(columnIndex);

                    switch (rowIndex) {
                        case 1 -> column.setDescription(cell.getStringCellValue());
                        case 2 -> column.setName(cell.getStringCellValue());
                        case 3 -> column.setType(cell.getStringCellValue());
                        case 4 -> column.setScope(cell.getStringCellValue());
                        case 5 -> column.setNullable(cell.getBooleanCellValue());
                        case 6 -> column.setLink(cell.getStringCellValue());
                        default -> throw new RuntimeException("Unintended behavior");
                    }
                });

                if (!column.isIgnore() && scope.in(column.getScope())) {
                    schema.addColumn(column);
                }
            });

            schema.columns().forEach(col -> System.out.printf("Asset column [idx = %d, name = %s, desc = %s, type = %s, array = %s, scope = %s, nullable = %s, link = %s]%n",
                    col.getColumnIndex(), col.getName(), col.getDescription(), col.getType().getAssetType(), col.isArray(), col.getScope(), col.isNullable(), col.getLink()));

            return schema;
        }).filter(AssetSchema::nonEmpty).toList();
    }

    public List<AssetSchema> getSchemas()
    {
        return schemas.stream().filter(s -> s.equalScope(scope)).toList();
    }

    public void saveSchema(final String path) throws IOException
    {
        for(final var schema : getSchemas())
        {
            FileUtils.forceMkdir(new File(path));
            Files.writeString(Path.of(path, schema.getSheetName() + ".proto"), schema.toString(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
    }

    public List<AssetRow> getRows(final AssetSchema schema)
    {
        final var sheet = sheets.stream()
                .filter(it -> it.getSheetName().contentEquals(schema.getSheetName()))
                .findAny()
                .orElseThrow();

        final var primary = new MutableObject<AssetRow>();

        return schema.columns().stream().map(column -> {
            final var assetRow = new AssetRow(column);

            IntStream.range(DATA_ROW_START_AT, schema.getEndOfRow()).forEach(rowNum -> {
                final var row = sheet.getRow(rowNum);
                final var cell = row.getCell(column.getColumnIndex());

                final var value = cell == null ? null : switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
                    case NUMERIC -> {
                        final var val = cell.getNumericCellValue();

                        yield switch (column.getType().getAssetType()) {
                            case ADouble.ASSET_TYPE -> Double.toString(val);
                            case AInteger.ASSET_TYPE -> Integer.toString((int) val);
                            case ALong.ASSET_TYPE -> Long.toString((long) val);
                            default -> throw new RuntimeException(String.format("Unprocessable cell type [type=%s]", cell.getCellType().name()));
                        };
                    }
                    case BLANK -> null;
                    default -> throw new RuntimeException(String.format("Unprocessable cell type [type=%s]", cell.getCellType().name()));
                };

                assetRow.put(rowNum, value != null ? value.strip() : null);
            });

            if (column.isPrimary()) {
                primary.set(assetRow);
            } else {
                assetRow.setPrimary(primary.get());
            }

            return assetRow;
        }).toList();
    }

    public void serialize(final String classPath, final String serializePath, final String aesKey) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InterruptedException
    {
        final var classLoader = new ProtobufClassLoader(classPath, packageName);
        final var schemas = getSchemas();

        for(final var schema : schemas)
        {
            final var rows = getRows(schema);
            final var schemaClass = classLoader.loadClass(packageName + "." + schema.getSheetName() + "OuterClass$" + schema.getSheetName());
            final var schemaDescriptorMethod = schemaClass.getDeclaredMethod("getDescriptor");
            final var schemaDescriptor = (Descriptors.Descriptor) schemaDescriptorMethod.invoke(null);
            final var schemaBuilder = DynamicMessage.newBuilder(schemaDescriptor);

            final var rowPerCol = (int) rows.stream()
                    .filter(row -> row.getName().contentEquals("code"))
                    .findAny()
                    .orElseThrow()
                    .stream()
                    .count();

            final var rowClass = classLoader.loadClass(packageName + "." + schema.getSheetName() + "OuterClass$" + schema.getSheetName() + "$Row");
            final var rowDescriptorMethod = rowClass.getDeclaredMethod("getDescriptor");

            for(var pos = 0; pos < rowPerCol; pos++)
            {
                final var rowDescriptor = (Descriptors.Descriptor) rowDescriptorMethod.invoke(null);
                final var rowBuilder = DynamicMessage.newBuilder(rowDescriptor);

                for(final var row : rows)
                {
                    if(row.getScope().not(scope))
                        continue;

                    final var fieldDescriptor = rowDescriptor.findFieldByName(row.getName());

                    if(fieldDescriptor.isRepeated())
                        row.asArray(pos).forEach(value -> rowBuilder.addRepeatedField(fieldDescriptor, value));
                    else
                        rowBuilder.setField(fieldDescriptor, row.asValue(pos));
                }

                schemaBuilder.addRepeatedField(schemaDescriptor.findFieldByName("row"), rowBuilder.build());
            }

            FileUtils.forceMkdir(new File(serializePath));

            if(aesKey != null)
            {
                final var out = new ByteArrayOutputStream();
                schemaBuilder.build().writeTo(out);

                Files.write(Path.of(serializePath, schema.getSheetName()), new AESCrypt(aesKey).encrypt(out.toByteArray()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            }
            else
            {
                schemaBuilder.build().writeTo(Files.newOutputStream(Path.of(serializePath, schema.getSheetName()),
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE));
            }
        }
    }
}
