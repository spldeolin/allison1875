# 名词解释

- `项目` Allision1875目前面向的是Maven项目，下文中的“项目”均指Maven项目
- `projectPath` 项目路径，如/Users/deolin/Documents/project-repo/allison1875
- `projectModules` 项目下的模块，如Allison1875目前有base、doc-analyzer、snippet-transformer、statute-inspector四个模块
- `sourceRoot` / `sourceRootPath` 项目或模块下的用户源码目录，一般是一个以src/main/java结尾的目录，类型是`SourceRoot` 或 `java.nio.Path`
- `classesPath` 项目或模块下用户源码编译后class文件所在的根目录路径，一般是一个以target/classes结尾的路径
- `externalJarPath` 项目或模块pom.xml中声明的依赖的jar文件的路径
- `sourceCode` 用户源码，指Java文件
- `AstForest` 抽象语法森林，是一个可遍历出`CompilationUnit`的单例对象，代表了`base-config.yml`中所有指定的`projects`和`projectModules`下`CompilationUnit`的集合，Allison1875所有源码工具的入口
- `Collector` 用于收集某类对象的收集器