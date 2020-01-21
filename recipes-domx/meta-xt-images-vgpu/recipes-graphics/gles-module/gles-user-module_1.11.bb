require gles-common.inc
require gles-user-module.inc

PREFERRED_VERSION_gles-module-egl-headers = "1.11"

DEPENDS += " \
    clang-native \
    wayland-protocols \
"
DEPENDS_remove = " \
    binutils-cross-aarch64 \
"
#    llvmpvr 

PROVIDES_remove = "virtual/opencl libopencl"
RPROVIDES_${PN}_remove = " \
    libopencl \
"
EXTRA_OEMAKE += "LIBCLANG_PATH=${STAGING_LIBDIR_NATIVE}/libclang.so"
#EXTRA_OEMAKE_remove = "LLVM_BUILD_DIR=${STAGING_LIBDIR}/llvm_build_dir"

EXCLUDED_APIS = ""
EXTRA_OEMAKE += "EXCLUDED_APIS='${EXCLUDED_APIS}'"
RDEPENDS_${PN} += "python3-core"


get_signatures () {
  local PREPARE_PATH=${WORKDIR}/git/build/linux/tools/prepare-llvm.sh
  local SIG1=`md5sum ${PREPARE_PATH} | awk '{print $1}'`
  local SIG2=`md5sum $(dirname ${PREPARE_PATH})/prepare-common.sh | awk '{print $1}'`
  BUILD_SIGNATURE=`(md5sum | awk '{print $1}') << EOF
  ${SIG1}
  ${SIG2}
EOF` # This has to be at the beginning of the line to be recognised by bash as EOF
  COMPILER_SIGNATURE="$(echo $ARCH_LABEL && ${CC} -v 2>&1 | perl -ne 'if(/^gcc version (\S+)/){@a=split /\./, $1; print "$a[0]\n"}')"
echo "sigcheck dir ${0}"
echo "sigcheck ${BUILD_SIGNATURE}"
}


do_compile() {
    get_signatures
    # Add signature files needed for LLVM build system.
    echo "${BUILD_SIGNATURE}" > ${WORKDIR}/recipe-sysroot/usr/lib/llvm_build_dir/llvm.src/.signature
    echo "${BUILD_SIGNATURE}" > ${S}/.signature
}
