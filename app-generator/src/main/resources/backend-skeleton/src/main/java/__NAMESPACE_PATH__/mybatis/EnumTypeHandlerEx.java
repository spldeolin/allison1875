package __NAMESPACE__.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;
import __NAMESPACE__.common.BaseEnum;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumTypeHandlerEx<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final BaseTypeHandler typeHandler;

    public EnumTypeHandlerEx(Class<E> enumType) {
        Objects.requireNonNull(enumType, "Type argument cannot be null");
        if (BaseEnum.class.isAssignableFrom(enumType)) {
            typeHandler = new EnumAncestorTypeHandler((Class) enumType);
        } else {
            typeHandler = new EnumTypeHandler(enumType);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        typeHandler.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return (E) typeHandler.getNullableResult(rs, columnName);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return (E) typeHandler.getNullableResult(rs, columnIndex);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return (E) typeHandler.getNullableResult(cs, columnIndex);
    }

}
