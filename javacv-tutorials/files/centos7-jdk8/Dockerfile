FROM centos:7.6.1810

RUN set -eux; \
    yum install -y \
        gzip \
        tar \
        binutils \
        freetype fontconfig \
    ;

ENV JAVA_HOME /usr/java/openjdk-8
ENV PATH $JAVA_HOME/bin:$PATH

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN set -eux; \
    \
    arch="$(objdump="$(command -v objdump)" && objdump --file-headers "$objdump" | awk -F '[:,]+[[:space:]]+' '$1 == "architecture" { print $2 }')"; \
    case "$arch" in \
        'i386:x86-64') \
            downloadUrl='https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_x64_linux_8u292b10.tar.gz'; \
            ;; \
        'aarch64') \
            downloadUrl='https://github.com/AdoptOpenJDK/openjdk8-upstream-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_aarch64_linux_8u292b10.tar.gz'; \
            ;; \
        *) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; \
    esac; \
    \
    curl -fL -o openjdk.tgz "$downloadUrl"; \
    curl -fL -o openjdk.tgz.asc "$downloadUrl.sign"; \
    \
    export GNUPGHOME="$(mktemp -d)"; \
# pre-fetch Andrew Haley's (the OpenJDK 8 and 11 Updates OpenJDK project lead) key so we can verify that the OpenJDK key was signed by it
# (https://github.com/docker-library/openjdk/pull/322#discussion_r286839190)
# we pre-fetch this so that the signature it makes on the OpenJDK key can survive "import-clean" in gpg
    gpg --batch --keyserver keyserver.ubuntu.com --recv-keys EAC843EBD3EFDB98CC772FADA5CD6035332FA671; \
# TODO find a good link for users to verify this key is right (https://mail.openjdk.java.net/pipermail/jdk-updates-dev/2019-April/000951.html is one of the only mentions of it I can find); perhaps a note added to https://adoptopenjdk.net/upstream.html would make sense?
# no-self-sigs-only: https://salsa.debian.org/debian/gnupg2/commit/c93ca04a53569916308b369c8b218dad5ae8fe07
    gpg --batch --keyserver keyserver.ubuntu.com --keyserver-options no-self-sigs-only --recv-keys CA5F11C6CE22644D42C6AC4492EF8D39DC13168F; \
    gpg --batch --list-sigs --keyid-format 0xLONG CA5F11C6CE22644D42C6AC4492EF8D39DC13168F \
        | tee /dev/stderr \
        | grep '0xA5CD6035332FA671' \
        | grep 'Andrew Haley'; \
    gpg --batch --verify openjdk.tgz.asc openjdk.tgz; \
    rm -rf "$GNUPGHOME"; \
    \
    mkdir -p "$JAVA_HOME"; \
    tar --extract \
        --file openjdk.tgz \
        --directory "$JAVA_HOME" \
        --strip-components 1 \
        --no-same-owner \
    ; \
    rm openjdk.tgz*; \
    \
    rm -rf "$JAVA_HOME/jre/lib/security/cacerts"; \
# see "update-ca-trust" script which creates/maintains this cacerts bundle
    ln -sT /etc/pki/ca-trust/extracted/java/cacerts "$JAVA_HOME/jre/lib/security/cacerts"; \
    \
# https://github.com/oracle/docker-images/blob/a56e0d1ed968ff669d2e2ba8a1483d0f3acc80c0/OracleJava/java-8/Dockerfile#L17-L19
    ln -sfT "$JAVA_HOME" /usr/java/default; \
    ln -sfT "$JAVA_HOME" /usr/java/latest; \
    for bin in "$JAVA_HOME/bin/"*; do \
        base="$(basename "$bin")"; \
        [ ! -e "/usr/bin/$base" ]; \
        alternatives --install "/usr/bin/$base" "$base" "$bin" 20000; \
    done; \
    \
# basic smoke test
    javac -version; \
    java -version