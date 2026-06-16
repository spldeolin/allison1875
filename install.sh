#!/usr/bin/env bash
#
# Allison 1875 CLI — Install / Uninstall
#
# Install (one-liner):
#   curl -fsSL https://raw.githubusercontent.com/spldeolin/allison1875/master/install.sh | bash
#
# Install a specific branch/tag:
#   curl -fsSL https://raw.githubusercontent.com/spldeolin/allison1875/master/install.sh | bash -s -- --branch 13.0
#
# Uninstall:
#   curl -fsSL https://raw.githubusercontent.com/spldeolin/allison1875/master/install.sh | bash -s -- --uninstall
#   (or simply: allison1875-uninstall)
#

set -euo pipefail

# ==================== Configuration ====================

REPO_URL="https://github.com/spldeolin/allison1875.git"
BRANCH="master"
INSTALL_LIB_DIR="/usr/local/lib/allison1875"
INSTALL_BIN_DIR="/usr/local/bin"
BIN_NAME="allison1875"
UNINSTALL_BIN_NAME="allison1875-uninstall"
CLI_MODULE="allison1875-cli"
CLONE_DIR=""
UNINSTALL=false

# ==================== Output helpers ====================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

info()  { echo -e "${GREEN}=>${NC} $*"; }
warn()  { echo -e "${YELLOW}=>${NC} $*"; }
error() { echo -e "${RED}=>${NC} $*" >&2; }
step()  { echo -e "${BLUE}==>${NC} ${BOLD}$*${NC}"; }

# ==================== Argument parsing ====================

while [[ $# -gt 0 ]]; do
    case "$1" in
        --branch|-b)
            BRANCH="$2"
            shift 2
            ;;
        --uninstall)
            UNINSTALL=true
            shift
            ;;
        *)
            error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# ==================== Uninstall ====================

do_uninstall() {
    step "Uninstalling Allison 1875 CLI"

    local removed=false

    if [ -f "${INSTALL_BIN_DIR}/${BIN_NAME}" ]; then
        rm -f "${INSTALL_BIN_DIR}/${BIN_NAME}"
        info "Removed ${INSTALL_BIN_DIR}/${BIN_NAME}"
        removed=true
    fi

    if [ -f "${INSTALL_BIN_DIR}/${UNINSTALL_BIN_NAME}" ]; then
        rm -f "${INSTALL_BIN_DIR}/${UNINSTALL_BIN_NAME}"
        info "Removed ${INSTALL_BIN_DIR}/${UNINSTALL_BIN_NAME}"
        removed=true
    fi

    if [ -d "${INSTALL_LIB_DIR}" ]; then
        rm -rf "${INSTALL_LIB_DIR}"
        info "Removed ${INSTALL_LIB_DIR}/"
        removed=true
    fi

    if [ "$removed" = true ]; then
        info "Allison 1875 CLI has been completely uninstalled."
    else
        warn "Nothing to uninstall — Allison 1875 CLI is not installed."
    fi
}

if [ "$UNINSTALL" = true ]; then
    do_uninstall
    exit 0
fi

# ==================== Prerequisites ====================

check_prerequisites() {
    step "Checking prerequisites"

    if ! command -v git &> /dev/null; then
        error "git is required but not found. Please install git first."
        exit 1
    fi

    if ! command -v java &> /dev/null; then
        error "java is required but not found. Please install JDK 21+."
        exit 1
    fi

    if ! command -v mvn &> /dev/null; then
        error "mvn is required but not found. Please install Maven 3.6+."
        exit 1
    fi

    local java_version
    java_version=$(java -version 2>&1 | head -1 | sed 's/.*"\(.*\)".*/\1/' | cut -d. -f1)
    if [ "$java_version" -lt 21 ] 2>/dev/null; then
        warn "JDK 21+ is recommended (detected: $java_version)"
    fi

    info "All prerequisites satisfied (git, java, mvn)"
}

# ==================== Clone ====================

clone_repo() {
    step "Cloning allison1875 (branch: ${BRANCH})"

    CLONE_DIR=$(mktemp -d)
    trap cleanup EXIT

    git clone --depth 1 --branch "${BRANCH}" --single-branch "${REPO_URL}" "${CLONE_DIR}" 2>&1 \
        | grep -E "^(Cloning|Receiving|Resolving)" || true

    info "Source downloaded to temporary directory"
}

cleanup() {
    if [ -n "${CLONE_DIR}" ] && [ -d "${CLONE_DIR}" ]; then
        rm -rf "${CLONE_DIR}"
    fi
}

# ==================== Build ====================

build_project() {
    step "Building project (this may take a few minutes)"

    cd "${CLONE_DIR}"

    local log_file
    log_file=$(mktemp)

    set +e
    mvn install -DskipTests -B -ntp > "$log_file" 2>&1
    local mvn_exit=$?
    set -e

    # Show filtered progress from the log
    awk '
    /^\[INFO\] --- .* ---/ { print "   " $0 }
    /^\[INFO\] Building [A-Za-z]/ { print "   " $0 }
    /^\[INFO\] BUILD SUCCESS/ { print "   " $0 }
    /^\[ERROR\]/ { print "   " $0 }
    ' "$log_file"

    if [ $mvn_exit -ne 0 ]; then
        error "Build failed (exit code: $mvn_exit). Last 30 lines:"
        tail -30 "$log_file" >&2
        rm -f "$log_file"
        exit 1
    fi

    rm -f "$log_file"
    info "Build succeeded"
}

# ==================== Install ====================

install_jar() {
    step "Installing CLI"

    local fat_jar="${CLONE_DIR}/${CLI_MODULE}/target/allison1875.jar"

    if [ ! -f "${fat_jar}" ]; then
        error "Fat jar not found at ${fat_jar}"
        error "Please verify the branch contains a complete build configuration."
        exit 1
    fi

    mkdir -p "${INSTALL_LIB_DIR}"
    cp -f "${fat_jar}" "${INSTALL_LIB_DIR}/${CLI_MODULE}.jar"
    info "Installed jar to ${INSTALL_LIB_DIR}/${CLI_MODULE}.jar"
}

install_scripts() {
    # Main executable wrapper
    local wrapper="${INSTALL_BIN_DIR}/${BIN_NAME}"
    cat > "${wrapper}" << 'EOF'
#!/usr/bin/env bash
exec java -jar /usr/local/lib/allison1875/allison1875-cli.jar "$@"
EOF
    chmod +x "${wrapper}"
    info "Installed ${wrapper}"

    # Uninstall script
    local uninstaller="${INSTALL_BIN_DIR}/${UNINSTALL_BIN_NAME}"
    cat > "${uninstaller}" << 'UNINSTALL_EOF'
#!/usr/bin/env bash
set -euo pipefail
echo "Uninstalling Allison 1875 CLI..."
rm -f /usr/local/bin/allison1875
rm -f /usr/local/bin/allison1875-uninstall
rm -rf /usr/local/lib/allison1875
echo "Done. Allison 1875 CLI has been removed."
UNINSTALL_EOF
    chmod +x "${uninstaller}"
    info "Installed ${uninstaller}"
}

# ==================== Verify ====================

verify() {
    echo ""
    if command -v "${BIN_NAME}" &> /dev/null; then
        info "Installation complete!"
        echo ""
        echo "  Usage:"
        echo "    ${BIN_NAME} --tool=<tool> --domain=<name> --config=<path/to/.allison1875.yml>"
        echo ""
        echo "  Uninstall:"
        echo "    ${UNINSTALL_BIN_NAME}"
        echo ""
    else
        warn "Installed, but '${BIN_NAME}' is not on your PATH."
        warn "Ensure ${INSTALL_BIN_DIR} is in your PATH variable."
    fi
}

# ==================== Main ====================

main() {
    echo ""
    echo -e "${BOLD}Allison 1875 CLI Installer${NC}"
    echo ""

    check_prerequisites
    clone_repo
    build_project
    install_jar
    install_scripts
    verify
}

main
