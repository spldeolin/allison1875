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

### Prerequisites

- JDK 21+
- Maven 3.6+
- Git

### Install

```bash
curl -fsSL https://raw.githubusercontent.com/spldeolin/allison1875/master/install.sh | bash
```

To install a specific branch or tag:

```bash
curl -fsSL https://raw.githubusercontent.com/spldeolin/allison1875/master/install.sh | bash -s -- --branch 13.0
```

### Usage

```bash
allison1875 --tool=<toolName> --config=/path/to/allison1875.yml [--domain=<name>]
```

### Uninstall

```bash
allison1875-uninstall
```

## Contributing

```bash
git clone https://github.com/spldeolin/allison1875.git
cd allison1875
mvn install -DskipTests
```

Run tests:

```bash
mvn test -pl allison1875-cli -am
```

See [`CLAUDE.md`](CLAUDE.md) for coding conventions, architecture overview, and development workflows.
