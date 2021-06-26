package boozilla.asset.excel.type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface AType {
    String getAssetType();
    String getProtoType();
    Object cast(final Object value);

    default List<Object> toArray(final Object value)
    {
        return Arrays.stream(((String) Objects.requireNonNullElse(value, "[]")).replaceAll("\\[", "")
                .replaceAll("]", "")
                .split("\\s*,\\s*"))
                .map(valueStr -> {
                    var startOffs = valueStr.startsWith("\"") ? 1 : 0;
                    var endOffs = valueStr.endsWith("\"") ? valueStr.length() - 1 : valueStr.length();

                    final var val = valueStr.substring(startOffs, endOffs);
                    return cast((val.isEmpty() || val.isBlank()) ? null : val);
                }).collect(Collectors.toUnmodifiableList());
    }
}
