# Allison 1875

> 「 *Dedicated to allison, I cannot say anymore.* 」

*Allison 1875* is a toolkit for ultra-low-code-intrusive analysis and transformation of Java source code.

She provides a variety of practical source code tools, enabling developers to have a better experience and higher productivity during development. Hers underlying foundation is based on transforming Java source code into Abstract Syntax Trees (AST).

## Features

*Allison 1875* provides the following tools for now.

### doc-analyzer

Analyze and generate API documentation for the web API implemented through *Spring Web MVC* standard annotations.

### handler-transformer

Transform convenient-to-code initialization blocks in the source code that adhere to the conventions established by *Allison 1875* into standard *Spring Web MVC* request methods. Ensure that the parameters and return values of these request methods accurately reflect the data structures, validation, comments, and other aspects in the transformation results accurately reflect the information specified by developers in the initialization blocks.

### persistence-generator

Generate the persistence layer source code based on the *MySQL* table structure, including *MyBatis* mapper, Entity, and the 'Design' class required by other *Allison 1875* tools. The generation results are idempotent and do not affect the code written by developers manually.

### query-transformer

Transform fluent-style expressions in the source code adhering to *Allison 1875* conventions (referred to as QueryChain) into standard *MyBatis* CRUD mapping methods and SQL statements. Ensure that the parameters, return values, internal implementations, and other aspects in the transformation results accurately reflect the information specified by developers in the QueryChain.

### star-transfomer

Transform convenient-to-code expressions in the source code adhering to *Allison 1875* conventions (referred to as StarChain) into source code composed of QueryChains and data assembly logic. After a subsequent transformation through query-transformer, transform into code, that used to query from a fact table and its dimension tables, and assemble data according to the one-to-one or one-to-many relationships specified by StarChain.

### sqlapi-generator

Generate Data Access Layer Exposed APIs by simply specifying SQL statements and the class names for the *Spring Web MVC* control layer, service layer, and *Mabatis* persistence layer.

## Quick Start

### Install

```shell
git clone git@github.com:spldeolin/allison1875.git
mvn install -f allison1875/pom.xml
```

### Setup

```xml
<dependency>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-support</artifactId>
    <version>THE-LATEST</version>
</dependency>
```

```xml
<!-- if this project base on Spring Boot -->
<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>THE-LATEST</version>
    <configuration>
        <basePackage>com.corp.proj.service</basePackage>
    </configuration>
</plugin>

<!-- if this project base on Satisficing -->
<plugin>
    <groupId>com.spldeolin.allison1875</groupId>
    <artifactId>allison1875-maven-plugin</artifactId>
    <version>THE-LATEST</version>
    <dependencies>
        <dependency>
            <groupId>com.spldeolin.allison1875</groupId>
            <artifactId>allison1875-satisficing</artifactId>
            <version>THE-LATEST</version>
        </dependency>
    </dependencies>
    <configuration>
        <basePackage>com.corp.proj.service</basePackage>
    </configuration>
</plugin>
```

### Execute

```shell
mvn allison1875-maven-plugin:doc-analyzer
mvn allison1875-maven-plugin:handler-transformer
mvn allison1875-maven-plugin:persistence-generator
mvn allison1875-maven-plugin:query-transformer
mvn allison1875-maven-plugin:star-transformer
```

## Contribution

Any PR, star, suggestion would be greatly appreciated.