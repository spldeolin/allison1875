#!/bin/zsh
set -e

echo "=========================================="
echo " Allison 1875 Integration Tests Runner"
echo "=========================================="

# 先安装最新代码到本地仓库
echo ""
echo "[Step 0] Installing latest artifacts..."
mvn install -DskipTests -f "$(dirname "$0")/pom.xml"

# Java 8 IT：运行除 java-record、jakarta-validation 之外的所有用例
echo ""
echo "[Step 1] Running Java 8 IT cases..."
export JENV_VERSION=1.8
export JAVA_HOME=$(jenv prefix)
echo "  JAVA_HOME: $JAVA_HOME"
mvn verify -pl allison1875-maven-plugin -am -Pit-java8 -f "$(dirname "$0")/pom.xml"

# Java 17+ IT：运行 java-record、jakarta-validation 用例
echo ""
echo "[Step 2] Running Java 17+ IT cases (switching to JDK 21)..."
export JENV_VERSION=21
export JAVA_HOME=$(jenv prefix)
echo "  JAVA_HOME: $JAVA_HOME"
mvn verify -pl allison1875-maven-plugin -am -Pit-java17 -f "$(dirname "$0")/pom.xml"

echo ""
echo "=========================================="
echo " All integration tests completed!"
echo "=========================================="
