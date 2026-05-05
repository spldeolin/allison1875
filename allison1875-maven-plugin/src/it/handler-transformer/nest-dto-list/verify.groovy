/*
 * nest-dto-list 集成测试验证脚本
 *
 * 验证嵌套 DTO 标注 @L 时，父类中的字段名被复数化（English.plural），
 * 且字段类型为 List<XxxDTO>。
 */

File controllerFile = new File(basedir, "src/main/java/com/example/controller/ClassroomController.java")
assert controllerFile.exists() : "ClassroomController.java should exist"
String controllerContent = controllerFile.text

// init 块移除
assert !controllerContent.contains('class Resp') : "inner class Resp should be removed"
assert !controllerContent.contains('class Student') : "nested class Student should be removed"
assert !controllerContent.contains('class Course') : "nested class Course should be removed"
assert controllerContent.contains("/get-classroom") : "Should contain URL"

// ========== Resp DTO ==========
File respDtoDir = new File(basedir, "src/main/java/com/example/dto/resp")
assert respDtoDir.exists() : "resp DTO directory should exist"

// 嵌套 Student DTO 生成为独立文件
def studentFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("Student") }
assert studentFiles.size() == 1 : "Should generate StudentDTO file, found: ${studentFiles*.name}"

String studentContent = studentFiles[0].text
assert studentContent.contains("studentName") : "StudentDTO should contain 'studentName'"
assert studentContent.contains("studentNo") : "StudentDTO should contain 'studentNo'"

// 嵌套 Course DTO 生成为独立文件
def courseFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("Course") }
assert courseFiles.size() == 1 : "Should generate CourseDTO file, found: ${courseFiles*.name}"

// Resp 主 DTO 中字段名应该被复数化（students, courses）
def respFiles = respDtoDir.listFiles().findAll { it.name.endsWith(".java") && it.name.contains("GetClassroom") && it.name.contains("Resp") }
assert respFiles.size() == 1 : "Should generate GetClassroomResp DTO, found: ${respFiles*.name}"

String respContent = respFiles[0].text
assert respContent.contains("classroomName") : "Resp DTO should contain 'classroomName'"
// @L 嵌套 DTO 的字段名应为复数（English.plural）
assert respContent.contains("students") : "Resp DTO should contain plural field name 'students' for @L Student"
assert respContent.contains("courses") : "Resp DTO should contain plural field name 'courses' for @L Course"
// 字段类型应为 List
assert respContent.contains("List") : "Resp DTO should contain List type for @L nested DTO fields"

println "[nest-dto-list] All assertions passed."
return true
