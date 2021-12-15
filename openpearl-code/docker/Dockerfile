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
        ca-certificates vim \
        autoconf automake cmake dpkg-dev file git make patch \
        libc-dev libc++-dev libgcc-10-dev libstdc++-10-dev  \
        dirmngr gnupg2 lbzip2 wget xz-utils libtinfo5 g++ && \
    rm -rf /var/lib/apt/lists/*	   	    	      
#
RUN apt-get -qq update 
RUN useradd -m -s /usr/bin/bash openpearl
WORKDIR /home/openpearl/openpearl-code/openpearl-code
RUN ./installPackages dev
RUN su - openpearl
RUN git clone https://git.code.sf.net/p/openpearl/code openpearl-code
COPY .config /home/openpearl/openpearl-code/openpearl-code/configuration
WORKDIR /home/openpearl/openpearl-code/openpearl-code
RUN make prepare
RUN make install


