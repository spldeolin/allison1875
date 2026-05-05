#!/usr/bin/env bash
set -e

BASEDIR="$(cd "$(dirname "$0")" && pwd)"

echo "=========================================="
echo " Allison 1875 Integration Tests Runner"
echo "=========================================="

# 安装最新代码到本地仓库
echo ""
echo "[Step 1/2] Installing latest artifacts..."
mvn install -DskipTests -f "$BASEDIR/pom.xml"

# 运行所有 IT case（Java 17+ case 已在 pom.xml 中排除）并生成覆盖率报告
echo ""
echo "[Step 2/2] Running integration tests + JaCoCo coverage..."
mvn verify -pl allison1875-maven-plugin -am -f "$BASEDIR/pom.xml"

echo ""
echo "=========================================="
echo " All integration tests completed!"
echo " Coverage: allison1875-maven-plugin/target/site/jacoco-aggregate/index.html"
echo "=========================================="