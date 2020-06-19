# 名词解释

- `项目` Allision1875目前面向的是Maven项目，下文中的“项目”均指Maven项目
- `projectPath` 项目路径，如/Users/deolin/Documents/project-repo/allison1875
- `sourceRoot` / `sourceRootPath` 项目或模块下的用户源码目录，一般是一个以src/main/java结尾的目录，类型是`SourceRoot` 或 `java.nio.Path`
- `sourceCode` 用户源码，指Java文件
- `AstForest` 抽象语法森林，是一个可遍历出`CompilationUnit`的单例对象，代表了`base-config.yml`中所有指定的`projects`和`projectModules`下`CompilationUnit`的集合，Allison1875所有源码工具的入口
- `Collector` 用于收集某类对象的收集器