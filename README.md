# Allison 1875

> 「 *Dedicated to allison, I cannot say anymore.* 」

A toolkit for ultra-low-code-intrusive analysis and transformation of Java source code based on AST (Abstract Syntax
Tree). It targets Spring Boot + MyBatis projects, generating boilerplate code from lightweight DSLs and database
schemas.

## Tools

| Tool                    | Description                                                |
|-------------------------|------------------------------------------------------------|
| `doc-analyzer`          | Generate API documentation from Spring Web MVC controllers |
| `handler-transformer`   | Transform DSL init blocks into Spring MVC request handlers |
| `persistence-generator` | Generate MyBatis persistence layer from MySQL DDL          |
| `query-transformer`     | Transform QueryChain DSL into MyBatis CRUD + SQL           |
| `star-transformer`      | Transform StarChain DSL into join queries + data assembly  |
| `form-generator`        | Generate full CRUD stack from YAML form DSL (composite)    |
| `app-generator`         | Generate a full-stack application from app.yml DSL         |

## Quick Start

```bash
git clone git@github.com:spldeolin/allison1875.git
mvn install -f allison1875/pom.xml -DskipTests
./install-cli.sh
```

```bash
allison1875 --tool=<toolName> --config=./.allison1875.yml [--domain=<name>]
```

## For AI Coding Agents

See [`CLAUDE.md`](CLAUDE.md) for comprehensive coding rules, architecture patterns, and development workflows.
