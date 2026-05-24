package __NAMESPACE__.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import __NAMESPACE__.common.BaseEnum;

public class EnumAncestorTypeHandler extends BaseTypeHandler<BaseEnum<String>> {

    private final Class<BaseEnum<String>> enumType;

    public EnumAncestorTypeHandler(Class<BaseEnum<String>> enumType) {
        Objects.requireNonNull(enumType, "Type argument cannot be null");
        this.enumType = enumType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BaseEnum<String> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    @Override
    public BaseEnum<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return rs.wasNull() ? null : of(code);
    }

    @Override
    public BaseEnum<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return rs.wasNull() ? null : of(code);
    }

    @Override
    public BaseEnum<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return cs.wasNull() ? null : of(code);
    }

    private BaseEnum<String> of(String code) {
        for (BaseEnum<String> enumConstant : enumType.getEnumConstants()) {
            if (enumConstant.getCode().equals(code)) {
                return enumConstant;
            }
        }
        return null;
    }

}
