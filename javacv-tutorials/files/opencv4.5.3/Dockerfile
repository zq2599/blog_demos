FROM bolingcavalry/centos7.6-jdk8:0.0.1

RUN echo "export LC_ALL=en_US.UTF-8"  >>  /etc/profile \
    && source /etc/profile

RUN set -eux; \
    yum install -y \
        make \
        gcc \
        gcc-c++ \
        gtk+-devel \
        gimp-devel \
        gimp-devel-tools \
        gimp-help-browser \
        zlib-devel \
        libtiff-devel \
        libjpeg-devel \
        libpng-devel \
        gstreamer-devel \
        libavc1394-devel \
        libraw1394-devel \
        libdc1394-devel \
        jasper-devel \
        jasper-utils \
        swig \
        python \
        libtool \
        nasm \
        build-essential \
        ant \
        unzip \
    ;

RUN set -eux; \
    curl -fL -o cmake-3.12.2-Linux-x86_64.tar.gz https://cmake.org/files/v3.12/cmake-3.12.2-Linux-x86_64.tar.gz \
    && tar -zxvf cmake-3.12.2-Linux-x86_64.tar.gz \
    && mv cmake-3.12.2-Linux-x86_64 cmake-3.12.2 \
    && ln -sf /cmake-3.12.2/bin/* /usr/bin; \
    curl -fL -o opencv-4.5.3.zip https://codeload.github.com/opencv/opencv/zip/4.5.3; \
    unzip opencv-4.5.3.zip; \
    rm -rf opencv-4.5.3.zip; \
    cd opencv-4.5.3; \
    mkdir build; \
    cd build; \
    cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local ..; \
    make; \
    make install; \
    cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -DBUILD_TESTS=OFF ..;\
    make -j8; \
    make install
