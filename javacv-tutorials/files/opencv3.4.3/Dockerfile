FROM bolingcavalry/centos7.6-jdk8:0.0.1

RUN echo "export LC_ALL=en_US.UTF-8"  >>  /etc/profile \
    && source /etc/profile

RUN set -eux; \
    yum install -y \
        make \
        cmake \
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
    curl -fL -o opencv-3.4.3.zip https://codeload.github.com/opencv/opencv/zip/3.4.3; \
    unzip opencv-3.4.3.zip; \
    rm -rf opencv-3.4.3.zip; \
    cd opencv-3.4.3; \
    mkdir build; \
    cd build; \
    cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local ..; \
    make; \
    make install; \
    cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -DBUILD_TESTS=OFF ..;\
    make -j8; \
    make install