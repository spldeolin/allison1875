# App Generator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build app-generator module that generates a fullstack application (Vue3 frontend + Spring Boot 2.7 backend) from a single app.yml DSL file.

**Architecture:** AppGenerator acts as an orchestrator — it copies skeleton projects, replaces placeholders, delegates CRUD code generation to form-generator, and produces a ready-to-build output directory with README.

**Tech Stack:** Java 17 (Allison1875 runtime), Spring Boot 2.7 (generated backend), Vue3 + Naive UI (generated frontend), Guice (DI), Jackson YAML (DSL parsing)

---

### Task 1: Extend AppDef and MenuDef Java Models

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/AppDef.java`
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/MenuDef.java`

- [ ] **Step 1: Update AppDef — add @NotEmpty to namespace, add validation pattern**

```java
package com.spldeolin.allison1875.appgenerator.dsl;

import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.constraint.UpperCamel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppDef {

    @NotEmpty
    @Pattern(regexp = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$")
    String namespace;

    @NotEmpty
    @UpperCamel
    String name;

    @NotEmpty
    String title;

    @NotEmpty
    @Valid
    List<@NotNull MenuDef> menus;

}
```

- [ ] **Step 2: Update MenuDef — add @NotNull @Valid on form, change order to Integer**

```java
package com.spldeolin.allison1875.appgenerator.dsl;

import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuDef {

    String group;

    String icon;

    Integer order;

    @NotNull
    @Valid
    FormDef form;

}
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/dsl/
git commit -m "feat(app-generator): extend AppDef and MenuDef models with validation"
```

---

### Task 2: Create backend-skeleton

**Files:**
- Create: `app-generator/src/main/resources/backend-skeleton/pom.xml`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/Application.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/JacksonConfig.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/WebMvcConfig.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/config/ThreadPoolConfig.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/webmvc/GlobalExceptionAdvice.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mybatis/EnumTypeHandlerEx.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/mybatis/EnumAncestorTypeHandler.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/trace/HttpBodyReportFilter.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/trace/HttpBodyReportFilterExclusion.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/trace/DefaultHttpBodyReportExclusion.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/id/SnowFlake.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/JsonUtils.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/PageUtils.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/util/TimeUtils.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/RequestResult.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/BaseEnum.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/java/__NAMESPACE_PATH__/common/BizException.java`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/application.yml`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/logback-spring.xml`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/static/.gitkeep`
- Create: `app-generator/src/main/resources/backend-skeleton/src/main/resources/mapper/.gitkeep`
- Create: `app-generator/src/main/resources/backend-skeleton/sql/.gitkeep`

- [ ] **Step 1: Create pom.xml**

Copy structure from satisficing pom, merge into single module, use placeholders. Key points:
- `groupId`: `__NAMESPACE__`
- `artifactId`: `__APP_NAME__-backend`  
- `<finalName>__APP_NAME__-fullstack</finalName>`
- Spring Boot 2.7.18 parent
- Dependencies: web, validation, aop, sleuth, jasypt, jackson-jsr310, mybatis, pagehelper, guava, commons-lang3, commons-collections4, commons-io, logstash-logback-encoder, mysql-connector, lombok
- Spring Boot Maven plugin for executable jar

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>__NAMESPACE__</groupId>
    <artifactId>__APP_NAME__-backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>2021.0.9</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.3.2</version>
        </dependency>
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>33.0.0-jre</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
            <version>4.4</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.16.1</version>
        </dependency>
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.3</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <finalName>__APP_NAME__-fullstack</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: Create Application.java**

```java
package __NAMESPACE__;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

- [ ] **Step 3: Copy and adapt config/ classes from satisficing**

Read each source file from `/Users/Deolin/Documents/project-repo/github/satisficing/satisficing-app/src/main/java/com/spldeolin/satisficing/app/config/` and adapt:
- Replace `package com.spldeolin.satisficing.app.config` → `package __NAMESPACE__.config`
- Keep all logic intact

- [ ] **Step 4: Copy and adapt webmvc/, mybatis/, trace/, id/, util/ classes**

Same approach — read each from satisficing, replace package names with `__NAMESPACE__.{subpackage}`.

For `GlobalExceptionAdvice.java`: update imports for `BizException` to point to `__NAMESPACE__.common.BizException`.

- [ ] **Step 5: Create common/ classes from satisficing-api**

- `RequestResult.java`: from `satisficing-api/src/main/java/.../api/RequestResult.java`, package → `__NAMESPACE__.common`
- `BaseEnum.java`: from `satisficing-api/src/main/java/.../api/BaseEnum.java`, package → `__NAMESPACE__.common`
- `BizException.java`: from `satisficing-app/src/main/java/.../app/exception/BizException.java`, package → `__NAMESPACE__.common`

- [ ] **Step 6: Create application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/__APP_NAME__?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      static-locations: classpath:/static/

logging:
  config: classpath:logback-spring.xml

mybatis:
  mapper-locations: classpath*:mapper/*.xml
  configuration:
    default-enum-type-handler: __NAMESPACE__.mybatis.EnumTypeHandlerEx
```

- [ ] **Step 7: Create logback-spring.xml**

Copy from satisficing's `logback-spring.xml`.

- [ ] **Step 8: Create placeholder directories with .gitkeep**

- `src/main/resources/static/.gitkeep`
- `src/main/resources/mapper/.gitkeep`
- `sql/.gitkeep`

- [ ] **Step 9: Commit**

```bash
git add app-generator/src/main/resources/backend-skeleton/
git commit -m "feat(app-generator): create backend-skeleton based on satisficing"
```

---

### Task 3: Refactor frontend-skeleton to use app.json

**Files:**
- Delete: `app-generator/src/main/resources/frontend-skeleton/plugins/vite-plugin-form-dsl.ts`
- Delete: `app-generator/src/main/resources/frontend-skeleton/src/dsl/student.yml`
- Delete: `app-generator/src/main/resources/frontend-skeleton/tests/plugins/vite-plugin-form-dsl.test.ts`
- Create: `app-generator/src/main/resources/frontend-skeleton/src/app.json`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/schema/types.ts`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/router/index.ts`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/layouts/DashboardLayout.vue`
- Modify: `app-generator/src/main/resources/frontend-skeleton/vite.config.ts`
- Modify: `app-generator/src/main/resources/frontend-skeleton/package.json`
- Modify: `app-generator/src/main/resources/frontend-skeleton/src/env.d.ts`

- [ ] **Step 1: Create src/app.json with example data**

```json
{
  "namespace": "com.example.demo",
  "name": "DemoApp",
  "title": "示例应用",
  "menus": [
    {
      "group": "示例管理",
      "icon": "GridOutline",
      "order": 1,
      "form": {
        "name": "DemoForm",
        "title": "示例表单",
        "items": [
          {
            "type": "text",
            "name": "demoField",
            "title": "示例字段",
            "isNonVoid": true,
            "initPattern": "userInput",
            "editPattern": "userInput",
            "maxLength": 100
          }
        ]
      }
    }
  ]
}
```

- [ ] **Step 2: Update src/schema/types.ts — add AppDef, MenuDef; remove group/icon/order from FormDef**

```typescript
export type InitOrEditPattern = 'doNot' | 'userInput' | 'todo'
export type TimeFormat = 'date' | 'time' | 'dateTime'

export interface OptionDef {
  code: string
  title: string
}

export interface IndexDef {
  itemNames: string[]
  isUnique: boolean
}

interface ItemDefBase {
  name: string
  title: string
  isNonVoid: boolean
  initPattern: InitOrEditPattern
  editPattern: InitOrEditPattern
}

export interface TextItemDef extends ItemDefBase {
  type: 'text'
  maxLength?: number
  isMultilineOrRich?: boolean
  regex?: string
}

export interface NumberItemDef extends ItemDefBase {
  type: 'number'
  canBeDecimal?: boolean
}

export interface SelectItemDef extends ItemDefBase {
  type: 'select'
  options: OptionDef[]
}

export interface MultiSelectItemDef extends ItemDefBase {
  type: 'multiSelect'
  options: OptionDef[]
}

export interface TimeItemDef extends ItemDefBase {
  type: 'time'
  format: TimeFormat
}

export interface OnOffItemDef extends ItemDefBase {
  type: 'onOff'
}

export interface SecretItemDef extends ItemDefBase {
  type: 'secret'
}

export type ItemDef =
  | TextItemDef
  | NumberItemDef
  | SelectItemDef
  | MultiSelectItemDef
  | TimeItemDef
  | OnOffItemDef
  | SecretItemDef

export interface FormDef {
  name: string
  title: string
  desc?: string
  items: ItemDef[]
  indices?: IndexDef[]
}

export interface MenuDef {
  group?: string
  icon?: string
  order?: number
  form: FormDef
}

export interface AppDef {
  namespace: string
  name: string
  title: string
  menus: MenuDef[]
}
```

- [ ] **Step 3: Update src/router/index.ts — import from app.json instead of virtual:form-dsl**

```typescript
import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import appDef from '@/app.json'
import type { AppDef } from '@/schema/types'
import { upperCamelToKebab } from '@/utils/naming'
import { useAuthStore } from '@/stores/auth'

const CrudPage = () => import('@/core/CrudPage.vue')
const DashboardLayout = () => import('@/layouts/DashboardLayout.vue')
const Login = () => import('@/views/Login.vue')

const app = appDef as AppDef

const dslRoutes: RouteRecordRaw[] = app.menus.map(menu => ({
  path: `/${upperCamelToKebab(menu.form.name)}`,
  name: menu.form.name,
  component: CrudPage,
  props: { schema: menu.form },
  meta: { title: menu.form.title, group: menu.group, icon: menu.icon, order: menu.order }
}))

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: DashboardLayout,
    meta: { requiresAuth: true },
    children: [
      ...dslRoutes,
      { path: '', redirect: dslRoutes.length > 0 ? dslRoutes[0].path : '/login' }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const requiresAuth = to.matched.some(r => r.meta.requiresAuth !== false)
  if (requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
```

- [ ] **Step 4: Update src/layouts/DashboardLayout.vue — read title from app.json**

In the `<script setup>` section, add:
```typescript
import appDef from '@/app.json'
import type { AppDef } from '@/schema/types'

const app = appDef as AppDef
```

In template, replace the hardcoded `<span class="logo-text">Form Web</span>` with:
```html
<span class="logo-text">{{ app.title }}</span>
```

- [ ] **Step 5: Update vite.config.ts — remove form-dsl plugin**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import mockApiPlugin from './plugins/vite-plugin-mock-api'

export default defineConfig({
  plugins: [
    vue(),
    mockApiPlugin()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5180,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'happy-dom'
  }
})
```

- [ ] **Step 6: Update package.json — remove js-yaml devDependencies**

Remove `"js-yaml": "4.1.0"` and `"@types/js-yaml": "4.0.9"` from devDependencies.

- [ ] **Step 7: Update src/env.d.ts — remove virtual:form-dsl declaration, add JSON module**

```typescript
/// <reference types="vite/client" />

declare module '*.json' {
  const value: any
  export default value
}
```

- [ ] **Step 8: Delete obsolete files**

```bash
rm app-generator/src/main/resources/frontend-skeleton/plugins/vite-plugin-form-dsl.ts
rm app-generator/src/main/resources/frontend-skeleton/src/dsl/student.yml
rm app-generator/src/main/resources/frontend-skeleton/tests/plugins/vite-plugin-form-dsl.test.ts
```

- [ ] **Step 9: Update remaining tests if they reference FormDef.group/icon/order**

Check `tests/schema/types.test.ts` — if it references `group`/`icon`/`order` on FormDef, update to use the new structure.

- [ ] **Step 10: Commit**

```bash
git add -A app-generator/src/main/resources/frontend-skeleton/
git commit -m "refactor(frontend-skeleton): replace form-dsl YAML parsing with app.json import"
```

---

### Task 4: Implement AppGenerator.process()

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`
- Create: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGeneratorModule.java`

- [ ] **Step 1: Create AppGeneratorModule**

```java
package com.spldeolin.allison1875.appgenerator;

import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import lombok.ToString;

@ToString
public class AppGeneratorModule extends Allison1875Module {

    private final Config config;

    public AppGeneratorModule(Config config) {
        this.config = config;
    }

    @Override
    public Class<? extends Allison1875MainService> declareMainService() {
        return AppGenerator.class;
    }

    @Override
    protected void configure() {
        bind(Config.class).toInstance(config);
    }

}
```

- [ ] **Step 2: Implement AppGenerator.process() — full orchestration logic**

```java
package com.spldeolin.allison1875.appgenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.appgenerator.dsl.AppDef;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class AppGenerator implements Allison1875MainService {

    @Inject
    private Config config;

    @Override
    public void process() {
        // 1. Parse app.yml
        AppDef appDef = parseAppDef();
        log.info("parsed AppDef: name={}, title={}, menus={}", appDef.getName(), appDef.getTitle(),
                appDef.getMenus().size());

        // 2. Determine output paths
        String appName = appDef.getName();
        Path outputRoot = config.getAppGeneratorOutputDir().toPath().resolve(appName);
        Path backendOutput = outputRoot.resolve(appName + "-backend");
        Path frontendOutput = outputRoot.resolve(appName + "-frontend");

        // 3. Generate backend
        generateBackend(appDef, backendOutput);

        // 4. Generate frontend
        generateFrontend(appDef, frontendOutput);

        // 5. Generate README.md
        generateReadme(appDef, outputRoot);

        log.info("app-generator completed. output={}", outputRoot.toAbsolutePath());
    }

    private AppDef parseAppDef() {
        try {
            String content = Files.readString(config.getAppDslPath().toPath(), StandardCharsets.UTF_8);
            return new YAMLMapper().readValue(content, AppDef.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void generateBackend(AppDef appDef, Path output) {
        // Copy skeleton
        Path skeleton = getResourcePath("backend-skeleton");
        copyDirectory(skeleton, output);

        // Replace placeholders in all files
        String namespace = appDef.getNamespace();
        String namespacePath = namespace.replace('.', '/');
        replaceInAllFiles(output, "__NAMESPACE__", namespace);
        replaceInAllFiles(output, "__NAMESPACE_PATH__", namespacePath);
        replaceInAllFiles(output, "__APP_NAME__", appDef.getName());
        replaceInAllFiles(output, "__APP_TITLE__", appDef.getTitle());

        // Rename __NAMESPACE_PATH__ directory
        Path placeholderDir = output.resolve("src/main/java/__NAMESPACE_PATH__");
        Path actualDir = output.resolve("src/main/java/" + namespacePath);
        try {
            Files.createDirectories(actualDir.getParent());
            Files.move(placeholderDir, actualDir);
            // Clean up empty parent directories left by __NAMESPACE_PATH__
            deleteEmptyParents(placeholderDir.getParent(), output.resolve("src/main/java"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Call form-generator for CRUD code (delegate to form-generator integration)
        List<FormDef> forms = appDef.getMenus().stream()
                .map(menu -> menu.getForm())
                .collect(Collectors.toList());
        log.info("delegating {} forms to form-generator", forms.size());
        // TODO: form-generator integration will be wired in Task 5
    }

    private void generateFrontend(AppDef appDef, Path output) {
        // Copy skeleton
        Path skeleton = getResourcePath("frontend-skeleton");
        copyDirectory(skeleton, output);

        // Write app.json
        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            String appJson = mapper.writeValueAsString(appDef);
            Files.writeString(output.resolve("src/app.json"), appJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void generateReadme(AppDef appDef, Path outputRoot) {
        String name = appDef.getName();
        try {
            String originalDsl = Files.readString(config.getAppDslPath().toPath(), StandardCharsets.UTF_8);
            String readme = "# " + appDef.getTitle() + "\n\n"
                    + "## 构建\n\n"
                    + "```bash\n"
                    + "cd " + name + "-frontend\n"
                    + "npm install\n"
                    + "npm run build\n"
                    + "cp -r dist/* ../" + name + "-backend/src/main/resources/static/\n"
                    + "cd ../" + name + "-backend\n"
                    + "mvn package\n"
                    + "```\n\n"
                    + "## 运行\n\n"
                    + "```bash\n"
                    + "java -jar " + name + "-backend/target/" + name + "-fullstack.jar\n"
                    + "```\n\n"
                    + "## DSL\n\n"
                    + "```yaml\n"
                    + originalDsl
                    + "\n```\n";
            Files.writeString(outputRoot.resolve("README.md"), readme, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getResourcePath(String resourceName) {
        try {
            var url = getClass().getClassLoader().getResource(resourceName);
            if (url == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDirectory(Path source, Path target) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void replaceInAllFiles(Path dir, String placeholder, String replacement) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && !file.getFileName().toString().equals(".gitkeep")) {
                        String content = Files.readString(file, StandardCharsets.UTF_8);
                        if (content.contains(placeholder)) {
                            Files.writeString(file, content.replace(placeholder, replacement), StandardCharsets.UTF_8);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void deleteEmptyParents(Path dir, Path stopAt) throws IOException {
        while (dir != null && !dir.equals(stopAt) && Files.isDirectory(dir)) {
            try (var entries = Files.list(dir)) {
                if (entries.findAny().isEmpty()) {
                    Files.delete(dir);
                    dir = dir.getParent();
                } else {
                    break;
                }
            }
        }
    }

}
```

- [ ] **Step 3: Add appGeneratorOutputDir and appDslPath fields to Config**

In `common/src/main/java/com/spldeolin/allison1875/common/config/Config.java`, add to the form-generator section or add a new app-generator section:

```java
// ==================== app-generator 配置 ====================

/**
 * App DSL 文件路径
 */
@NotNull
File appDslPath = new File("./app.yml");

/**
 * app-generator 输出目录
 */
@NotNull
File appGeneratorOutputDir = new File("./output");
```

- [ ] **Step 4: Register app-generator in ToolEnum**

Add to `common/src/main/java/com/spldeolin/allison1875/common/enums/ToolEnum.java`:

```java
APP_GENERATOR("app-generator", Config::getAppGeneratorModule, false),
```

Add to `Config.java`:

```java
/** app-generator 功能所使用的 Guice Module 实现类全限定名 */
String appGeneratorModule = "com.spldeolin.allison1875.appgenerator.AppGeneratorModule";
```

- [ ] **Step 5: Commit**

```bash
git add app-generator/src/main/java/ common/src/main/java/
git commit -m "feat(app-generator): implement AppGenerator orchestration and register in ToolEnum"
```

---

### Task 5: Wire form-generator Integration in AppGenerator

**Files:**
- Modify: `app-generator/src/main/java/com/spldeolin/allison1875/appgenerator/AppGenerator.java`

- [ ] **Step 1: Add form-generator delegation logic**

Replace the TODO in `generateBackend()` with actual form-generator invocation. The approach:
1. Construct a Config for form-generator (reuse the current config, set dslPath to a temp file containing the forms list)
2. Construct a DomainConfig pointing at the generated backend output directory
3. Set DomainContext
4. Call `Allison1875.letsGo(ToolEnum.FORM_GENERATOR, ...)` or directly instantiate and invoke FormGenerator via Guice

Since form-generator is a composite tool that internally calls persistence-generator, handler-transformer, etc., and reads forms from `config.getDslPath()`, the integration approach is:

```java
private void invokeFormGenerator(AppDef appDef, Path backendOutput, List<FormDef> forms) {
    // Serialize forms to a temp YAML file for form-generator to read
    Path tempDsl = writeTempFormsDsl(forms);

    // Construct config for form-generator
    Config fgConfig = new Config();
    fgConfig.setDslPath(tempDsl.toFile());
    fgConfig.setJavaVersion("1.8");
    fgConfig.setAuthor("app-generator");
    fgConfig.setEnableDocAnalyzer(false);
    fgConfig.setJdbcUrl(null);
    fgConfig.setEnableGenerateDesign(true);
    fgConfig.setIsEntityEndWithEntity(true);
    fgConfig.setEnableJavaxMoveToJakarta(false);
    fgConfig.setPageParamStyle(PageParamStyleEnum.PAGE_NO_PAGE_SIZE);

    // Set code snippets for the generated backend
    Config.CodeSnippet cs = new Config.CodeSnippet();
    String ns = appDef.getNamespace();
    cs.setRequestResultQualifier(ns + ".common.RequestResult");
    cs.setRequestResultTypeDeclaration("RequestResult<${dataType}>");
    cs.setRequestResultSuccessNoData("RequestResult.success()");
    cs.setRequestResultSuccessWithData("RequestResult.success(${data})");
    cs.setConstructPageResult("new PageResult<>(${total}, ${dtos})");
    cs.setConstructEmptyPageResult("new PageResult<>(0L, Collections.emptyList())");
    fgConfig.setCodeSnippet(cs);

    // Construct DomainConfig pointing to the generated backend
    String absPath = backendOutput.toAbsolutePath().toString();
    DomainConfig dc = new DomainConfig();
    dc.setName("default");
    dc.setControllerModule(absPath);
    dc.setControllerPackage(ns + ".controller");
    dc.setDtoModule(absPath);
    dc.setReqDTOPackage(ns + ".controller");
    dc.setRespDTOPackage(ns + ".controller");
    dc.setEnumModule(absPath);
    dc.setEnumPackage(ns + ".enums");
    dc.setServiceModule(absPath);
    dc.setServicePackage(ns + ".service");
    dc.setServiceImplModule(absPath);
    dc.setServiceImplPackage(ns + ".service.impl");
    dc.setPersistenceModule(absPath);
    dc.setMapperPackage(ns + ".mapper");
    dc.setEntityPackage(ns + ".entity");
    dc.setDesignPackage(ns + ".design");
    dc.setParamDTOPackage(ns + ".mapper");
    dc.setRecordDTOPackage(ns + ".mapper");
    dc.setWholeDTOPackage(ns + ".controller");
    fgConfig.setDomains(Lists.newArrayList(dc));

    // Invoke form-generator
    Allison1875.letsGo(ToolEnum.FORM_GENERATOR, fgConfig, null);

    // Cleanup temp file
    try { Files.deleteIfExists(tempDsl); } catch (IOException ignored) {}
}

private Path writeTempFormsDsl(List<FormDef> forms) {
    try {
        Path temp = Files.createTempFile("app-generator-forms-", ".yml");
        String yaml = new YAMLMapper().writeValueAsString(forms);
        Files.writeString(temp, yaml, StandardCharsets.UTF_8);
        return temp;
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
}
```

- [ ] **Step 2: Add necessary imports and call invokeFormGenerator from generateBackend**

Replace the TODO comment with:
```java
invokeFormGenerator(appDef, output, forms);
```

- [ ] **Step 3: Commit**

```bash
git add app-generator/src/main/java/
git commit -m "feat(app-generator): wire form-generator integration for CRUD code generation"
```

---

### Task 6: Superpowers Directory Migration (Cleanup)

**Files:**
- Verify: `docs/superpowers/specs/2026-05-23-form-web-design.md` (already migrated)
- Verify: `docs/superpowers/plans/2026-05-23-form-web-implementation.md` (already migrated)
- Verify: `app-generator/src/main/resources/frontend-skeleton/docs/` directory is deleted

- [ ] **Step 1: Verify migration is complete**

```bash
ls docs/superpowers/specs/ docs/superpowers/plans/
# Should show the migrated files plus the new spec
test ! -d app-generator/src/main/resources/frontend-skeleton/docs && echo "OK: docs removed"
```

- [ ] **Step 2: If still staged from prior work, ensure clean state**

The superpowers migration was already committed in the spec-writing phase. Verify with `git log --oneline -1`.

---

### Task 7: Integration Verification

**Files:**
- No new files — this is a verification task

- [ ] **Step 1: Compile app-generator module**

```bash
cd /Users/Deolin/Documents/project-repo/github/allison1875
mvn compile -pl app-generator -am -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Verify backend-skeleton files contain correct placeholders**

```bash
grep -r "__NAMESPACE__" app-generator/src/main/resources/backend-skeleton/ | head -5
grep -r "__APP_NAME__" app-generator/src/main/resources/backend-skeleton/ | head -5
```

Expected: placeholders found in pom.xml, application.yml, Java files

- [ ] **Step 3: Verify frontend-skeleton no longer references form-dsl**

```bash
grep -r "form-dsl\|virtual:form-dsl\|js-yaml" app-generator/src/main/resources/frontend-skeleton/ || echo "OK: no references"
```

Expected: "OK: no references"

- [ ] **Step 4: Final commit (if any fixups needed)**

```bash
git status
# If there are fixes, commit them
```
