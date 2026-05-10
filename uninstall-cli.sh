#!/usr/bin/env bash
#
# Allison 1875 CLI 卸载脚本
#
# 功能：
#   1. 删除 /usr/local/bin/allison1875 可执行包装脚本
#   2. 删除 /usr/local/lib/allison1875/ 目录及其中所有文件
#
# 用法：
#   ./uninstall-cli.sh
#

set -euo pipefail

# ==================== 配置 ====================

INSTALL_LIB_DIR="/usr/local/lib/allison1875"
INSTALL_BIN="/usr/local/bin/allison1875"

# ==================== 颜色输出 ====================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ==================== 卸载 ====================

remove_bin() {
    if [ -f "${INSTALL_BIN}" ]; then
        rm -f "${INSTALL_BIN}"
        info "已删除 ${INSTALL_BIN}"
    else
        warn "${INSTALL_BIN} 不存在，跳过"
    fi
}

remove_lib() {
    if [ -d "${INSTALL_LIB_DIR}" ]; then
        rm -rf "${INSTALL_LIB_DIR}"
        info "已删除 ${INSTALL_LIB_DIR}/"
    else
        warn "${INSTALL_LIB_DIR}/ 不存在，跳过"
    fi
}

# ==================== 主流程 ====================

main() {
    echo ""
    echo "========================================"
    echo "  Allison 1875 CLI 卸载脚本"
    echo "========================================"
    echo ""

    remove_bin
    remove_lib

    info "卸载完成，无残留。"
}

main "$@"
