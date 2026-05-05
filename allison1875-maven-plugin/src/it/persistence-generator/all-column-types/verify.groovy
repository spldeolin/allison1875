/*
 * persistence-generator all-column-types 集成测试验证脚本
 *
 * 验证所有支持的列类型正确映射到 Java 类型
 */

File entityFile = new File(basedir, "src/main/java/com/example/entity/TAllTypesEntity.java")
assert entityFile.exists() : "TAllTypesEntity.java should be generated"
String entityContent = entityFile.text

// tinyint(1) -> Boolean
assert entityContent.contains("Boolean") : "tinyint(1) should map to Boolean"
assert entityContent.contains("colBoolean") : "col_boolean should map to colBoolean"

// tinyint (non-boolean) -> Byte
assert entityContent.contains("Byte") : "tinyint should map to Byte"
assert entityContent.contains("colTinyint") : "col_tinyint should map to colTinyint"

// int -> Integer
assert entityContent.contains("Integer") : "int should map to Integer"
assert entityContent.contains("colInt") : "col_int should map to colInt"

// bigint -> Long
assert entityContent.contains("Long") : "bigint should map to Long"

// double -> Double
assert entityContent.contains("Double") : "double should map to Double"
assert entityContent.contains("colDouble") : "col_double should map to colDouble"

// decimal -> BigDecimal
assert entityContent.contains("BigDecimal") : "decimal should map to BigDecimal"
assert entityContent.contains("colDecimal") : "col_decimal should map to colDecimal"

// varchar, char, text, mediumtext -> String
assert entityContent.contains("colVarchar") : "col_varchar should map to colVarchar"
assert entityContent.contains("colChar") : "col_char should map to colChar"
assert entityContent.contains("colText") : "col_text should map to colText"
assert entityContent.contains("colMediumtext") : "col_mediumtext should map to colMediumtext"

// date -> LocalDate
assert entityContent.contains("LocalDate") : "date should map to LocalDate"
assert entityContent.contains("colDate") : "col_date should map to colDate"

// time -> LocalTime
assert entityContent.contains("LocalTime") : "time should map to LocalTime"
assert entityContent.contains("colTime") : "col_time should map to colTime"

// datetime, timestamp -> LocalDateTime
assert entityContent.contains("LocalDateTime") : "datetime/timestamp should map to LocalDateTime"
assert entityContent.contains("colDatetime") : "col_datetime should map to colDatetime"
assert entityContent.contains("colTimestamp") : "col_timestamp should map to colTimestamp"

println "[all-column-types] Entity type mapping assertions passed."

// 验证 Mapper XML resultMap 包含所有列
File xmlFile = new File(basedir, "src/main/resources/mapper/TAllTypesMapper.xml")
assert xmlFile.exists() : "TAllTypesMapper.xml should be generated"
String xmlContent = xmlFile.text
assert xmlContent.contains("col_boolean") : "XML should contain col_boolean"
assert xmlContent.contains("col_tinyint") : "XML should contain col_tinyint"
assert xmlContent.contains("col_decimal") : "XML should contain col_decimal"
assert xmlContent.contains("col_text") : "XML should contain col_text"
assert xmlContent.contains("col_date") : "XML should contain col_date"
assert xmlContent.contains("col_time") : "XML should contain col_time"
assert xmlContent.contains("col_timestamp") : "XML should contain col_timestamp"

println "[persistence-generator/all-column-types] All assertions passed."
return true
