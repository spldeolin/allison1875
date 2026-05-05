/*
 * persistence-generator disable-design 集成测试验证脚本
 *
 * 验证 enableGenerateDesign=false 时，不生成 Design 和 JoinChain 文件
 */

// Entity 和 Mapper 仍应正常生成
File entityFile = new File(basedir, "src/main/java/com/example/entity/TSimpleEntity.java")
assert entityFile.exists() : "TSimpleEntity.java should still be generated"

File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TSimpleMapper.java")
assert mapperFile.exists() : "TSimpleMapper.java should still be generated"

File xmlFile = new File(basedir, "src/main/resources/mapper/TSimpleMapper.xml")
assert xmlFile.exists() : "TSimpleMapper.xml should still be generated"

println "[disable-design] Entity/Mapper/XML still generated."

// Design 目录不应包含任何 Java 文件
File designDir = new File(basedir, "src/main/java/com/example/design")
if (designDir.exists()) {
    def designFiles = designDir.listFiles()?.findAll { it.name.endsWith(".java") }
    assert designFiles == null || designFiles.size() == 0 : "Design directory should be empty when enableGenerateDesign=false"
}

println "[disable-design] No Design files generated."

println "[persistence-generator/disable-design] All assertions passed."
return true
