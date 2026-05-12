#!/usr/bin/env bash
#
# Allison 1875 CLI 安装脚本
#
# 功能：
#   1. 编译并安装整个 allison1875 项目到本地 Maven 仓库
#   2. 将 allison1875-cli fat jar 安装到 /usr/local/lib/allison1875/
#   3. 在 /usr/local/bin/ 创建 allison1875 可执行包装脚本
#
# 用法：
#   ./install-cli.sh
#
# 前置条件：
#   - JDK 8+
#   - Maven 3.6+
#   - 当前目录为 allison1875 项目根目录
#

set -euo pipefail

# ==================== 配置 ====================

INSTALL_LIB_DIR="/usr/local/lib/allison1875"
INSTALL_BIN_DIR="/usr/local/bin"
INSTALL_BIN_NAME="allison1875"
CLI_MODULE="allison1875-cli"
CLI_ARTIFACT_ID="allison1875-cli"

# ==================== 颜色输出 ====================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ==================== 检查前置条件 ====================

check_prerequisites() {
    # 检查是否在项目根目录
    if [ ! -f "pom.xml" ] || [ ! -d "${CLI_MODULE}" ]; then
        error "请在 allison1875 项目根目录下执行此脚本"
        exit 1
    fi

    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        error "未找到 mvn 命令，请先安装 Maven"
        exit 1
    fi

    # 检查 Java
    if ! command -v java &> /dev/null; then
        error "未找到 java 命令，请先安装 JDK"
        exit 1
    fi

    info "前置条件检查通过"
}

# ==================== 编译安装 ====================

build_project() {
    info "开始编译并安装整个 allison1875 项目..."
    mvn install -DskipTests -q
    info "项目编译安装完成"
}

# ==================== 查找 fat jar ====================

find_fat_jar() {
    # maven-shade-plugin 会将原始 jar 重命名为 original-xxx.jar，shade 后的 fat jar 为 xxx.jar
    FAT_JAR="${CLI_MODULE}/target/allison1875.jar"

    if [ ! -f "${FAT_JAR}" ]; then
        error "未找到 fat jar: ${FAT_JAR}"
        error "请确认 maven-shade-plugin 已正确配置"
        exit 1
    fi

    info "找到 fat jar: ${FAT_JAR}"
}

# ==================== 安装 fat jar ====================

install_jar() {
    info "安装 fat jar 到 ${INSTALL_LIB_DIR}/ ..."
    mkdir -p "${INSTALL_LIB_DIR}"
    cp -f "${FAT_JAR}" "${INSTALL_LIB_DIR}/${CLI_ARTIFACT_ID}.jar"
    info "fat jar 已安装到 ${INSTALL_LIB_DIR}/${CLI_ARTIFACT_ID}.jar"
}

# ==================== 创建可执行包装脚本 ====================

install_wrapper_script() {
    local wrapper="${INSTALL_BIN_DIR}/${INSTALL_BIN_NAME}"
    info "创建可执行脚本 ${wrapper} ..."

    cat > "${wrapper}" << 'WRAPPER_EOF'
#!/usr/bin/env bash
#
# Allison 1875 CLI 包装脚本
# 自动生成，请勿手动修改
#
exec java -jar /usr/local/lib/allison1875/allison1875-cli.jar "$@"
WRAPPER_EOF

    chmod +x "${wrapper}"
    info "可执行脚本已安装到 ${wrapper}"
}

# ==================== 验证安装 ====================

verify_installation() {
    if command -v "${INSTALL_BIN_NAME}" &> /dev/null; then
        info "安装完成！可以通过以下命令使用："
        echo ""
        echo "  ${INSTALL_BIN_NAME} --tool=doc-analyzer --domain=<domainName> --config=<path/to/.allison1875.yml>"
        echo ""
    else
        warn "命令 '${INSTALL_BIN_NAME}' 不在 PATH 中，请确认 ${INSTALL_BIN_DIR} 已加入 PATH 环境变量"
    fi
}

# ==================== 主流程 ====================

main() {
    echo ""
    echo "========================================"
    echo "  Allison 1875 CLI 安装脚本"
    echo "========================================"
    echo ""

    check_prerequisites
    build_project
    find_fat_jar
    install_jar
    install_wrapper_script
    verify_installation

    info "全部完成！"
}

main "$@"
