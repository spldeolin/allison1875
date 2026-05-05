/*
 * persistence-generator entity-no-suffix 集成测试验证脚本
 *
 * 验证 isEntityEndWithEntity=false 时，Entity 类名不带 Entity 后缀
 */

// 类名应该是 TCategory 而不是 TCategoryEntity
File entityFile = new File(basedir, "src/main/java/com/example/entity/TCategory.java")
assert entityFile.exists() : "TCategory.java should be generated (without Entity suffix)"

// 确认没有生成带 Entity 后缀的文件
File entityWithSuffix = new File(basedir, "src/main/java/com/example/entity/TCategoryEntity.java")
assert !entityWithSuffix.exists() : "TCategoryEntity.java should NOT exist when isEntityEndWithEntity=false"

String entityContent = entityFile.text
assert entityContent.contains("class TCategory") : "Class should be named TCategory"
assert entityContent.contains("package com.example.entity") : "Should have correct package"

println "[entity-no-suffix] Entity naming assertions passed."

// Mapper 中引用的类型也应该是 TCategory
File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TCategoryMapper.java")
assert mapperFile.exists() : "TCategoryMapper.java should be generated"
String mapperContent = mapperFile.text
assert mapperContent.contains("TCategory") : "Mapper should reference TCategory (not TCategoryEntity)"

println "[entity-no-suffix] Mapper assertions passed."

// XML 中的 resultMap type 也应该是不带 Entity 后缀的类
File xmlFile = new File(basedir, "src/main/resources/mapper/TCategoryMapper.xml")
assert xmlFile.exists() : "TCategoryMapper.xml should be generated"
String xmlContent = xmlFile.text
assert xmlContent.contains("com.example.entity.TCategory") : "XML resultMap should use com.example.entity.TCategory"

println "[persistence-generator/entity-no-suffix] All assertions passed."
return true
