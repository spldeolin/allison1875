#!/usr/bin/env bash
set -e

BASEDIR="$(cd "$(dirname "$0")" && pwd)"

# 第一个参数或环境变量 INVOKER_IT_PROFILE：invoker-it-java8 | invoker-it-java17
INVOKER_PROFILE="${1:-${INVOKER_IT_PROFILE:-invoker-it-java8}}"

echo "=========================================="
echo " Allison 1875 Integration Tests Runner"
echo " Profile: $INVOKER_PROFILE"
echo "=========================================="

# 安装最新代码到本地仓库
echo ""
echo "[Step 1/2] Installing latest artifacts..."
mvn install -DskipTests -f "$BASEDIR/pom.xml"

# 运行 IT（须显式 profile，见 allison1875-maven-plugin/pom.xml）
echo ""
echo "[Step 2/2] Running integration tests + JaCoCo coverage..."
mvn verify -pl allison1875-maven-plugin -am -f "$BASEDIR/pom.xml" -P"$INVOKER_PROFILE"

echo ""
echo "=========================================="
echo " All integration tests completed!"
echo " Coverage: allison1875-maven-plugin/target/site/jacoco-aggregate/index.html"
echo "=========================================="