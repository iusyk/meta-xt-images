DESCRIPTION = "The Low Level Virtual Machine"
HOMEPAGE = "http://llvm.org"

# 3-clause BSD-like
# University of Illinois/NCSA Open Source License
LICENSE = "NCSA"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=c520ed40e11887bb1d24d86f7f5b1f05"

inherit cmake pythonnative

FILESEXTRAPATHS_prepend := "${THISDIR}/llvm:"
RDEPENDS_${PN} += "ncurses ncurses-libtinfo libgcc libstdc++ glibc"
DEPENDS += " \
    clang \
"
PREFERRED_VERSION_clang = "7.0.1"

SRC_URI = "http://llvm.org/releases/${PV}/llvm-${PV}.src.tar.xz;name=llvm \
           http://llvm.org/releases/${PV}/cfe-${PV}.src.tar.xz;name=cfe \
"

SRC_URI[cfe.md5sum] = "8583c9fb2af0ce61a7154fd9125363c1"
SRC_URI[cfe.sha256sum] = "a45b62dde5d7d5fdcdfa876b0af92f164d434b06e9e89b5d0b1cbc65dfe3f418"

SRC_URI[llvm.md5sum] = "79f1256f97d52a054da8660706deb5f6"
SRC_URI[llvm.sha256sum] = "a38dfc4db47102ec79dcc2aa61e93722c5f6f06f0a961073bd84b78fb949419b"

S = "${WORKDIR}/llvm-${PV}.src"
LLVM_BUILD_DIR = "${WORKDIR}/llvm.${TARGET_ARCH}"

EXTRA_OEMAKE += "CROSS_COMPILE=${TARGET_PREFIX}"

move_cfe() {
    if [ -d ${WORKDIR}/cfe-${PV}.src ]; then
       mv ${WORKDIR}/cfe-${PV}.src ${S}/tools/clang
    fi
}

python do_unpack_append() {
    bb.build.exec_func("move_cfe", d)
}

do_configure_prepend() {
    # Fails to build unless using separate directory from source
    mkdir -p ${LLVM_BUILD_DIR}
    cd ${LLVM_BUILD_DIR}
}

do_configure_append() {
    LLVM_BUILD_ARCH=$(${BUILD_CC} -v 2>&1 | awk '/^Target:/{print $2}')
    if [ ! -f BuildTools/Makefile ]; then
         mkdir -p BuildTools
         cd BuildTools
         unset CFLAGS
         unset CXXFLAGS
         AR=${BUILD_AR}
         AS=${BUILD_AS}
         LD=${BUILD_LD}
         CC=${BUILD_CC}
         CXX=${BUILD_CXX}
         unset SDKROOT
         unset UNIVERSAL_SDK_PATHi
         unset CC
         unset CXX
         unset M4
         cd ..
    fi

}

do_compile() {
        
    cd ${LLVM_BUILD_DIR}
    cmake ../llvm-${PV}.src 

    make clang-tblgen llvm-tblgen

    # Add signature files needed for LLVM build system.
    BUILD_SIGNATURE="fd4ef629f7b5430669d6476d847b1047"
    # expected compiler_signature is x86_64 7
    COMPILER_SIGNATURE=$(${TARGET_PREFIX}gcc -v 2>&1 | awk '/^Target:/{print $2};/^gcc version/{print $3}')
    echo "${BUILD_SIGNATURE}" > ${LLVM_BUILD_DIR}/.signature
    echo "${BUILD_SIGNATURE}" > ${S}/.signature
    echo "${COMPILER_SIGNATURE}" > ${LLVM_BUILD_DIR}/.compiler_signature
}

do_install() {
    if [ -d ${WORKDIR}/llvm-${PV}.src ];then
        install -d ${D}${libdir}/llvm_build_dir
        cp -rf ${S} ${D}${libdir}/llvm_build_dir
        mv ${D}${libdir}/llvm_build_dir/llvm-${PV}.src ${D}${libdir}/llvm_build_dir/llvm.src
        cp -rf ${LLVM_BUILD_DIR}  ${D}${libdir}/llvm_build_dir
    fi
}

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INSANE_SKIP_${PN} += "ldflags split-strip arch staticdev file-rdeps"
FILES_${PN} += "${libdir}/llvm_build_dir/"
