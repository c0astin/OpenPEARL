##############################################################################
# 
# Run in the directory of this dockerfile
#
#        docker build -t openpearl .
#
# This command builds an docker image with all the necessary packages and setup.
#
# To test the successful installation runtime
#
#       docker run -it openpearl
#       su - openpearl
#       cd openpearl-code/openpearl-code/testsuite/build
#       prl 220_philosophers.prl
#       ./220_philosophers.prl
#
##############################################################################

FROM ubuntu
RUN apt-get -qq update 
RUN DEBIAN_FRONTEND="noninteractive" TZ="Europe/Berlin" apt-get install -y tzdata

# Install dependencies
RUN apt-get install -qqy --no-install-recommends \
        ca-certificates vim locales \
        autoconf automake cmake dpkg-dev file git make patch \
        libc-dev libc++-dev libgcc-10-dev libstdc++-10-dev  \
        dirmngr gnupg2 lbzip2 wget xz-utils libtinfo5 g++ && \
    rm -rf /var/lib/apt/lists/*	   	    	      
#
RUN apt-get -qq update 


# configure locale
RUN touch /usr/share/locale/locale.alias 
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
RUN locale-gen

# add a non-root user 
RUN useradd -m -s /usr/bin/bash openpearl

# clone the openpearl git repository
WORKDIR /home/openpearl
RUN git clone https://git.code.sf.net/p/openpearl/code openpearl-code

# install the missing packages
WORKDIR /home/openpearl/openpearl-code/openpearl-code
RUN ./installPackages dev

# fix permissions
RUN cd /home/openpearl ; chown -R openpearl:openpearl openpearl-code

# change to  non-root user and perform the rest of the installation
RUN su - openpearl
COPY .config /home/openpearl/openpearl-code/openpearl-code/configuration
WORKDIR /home/openpearl/openpearl-code/openpearl-code
RUN make prepare
RUN make install



