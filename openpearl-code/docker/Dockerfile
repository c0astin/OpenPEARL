FROM ubuntu

RUN apt-get -qq update 
RUN DEBIAN_FRONTEND="noninteractive" TZ="Europe/Berlin" apt-get install -y tzdata
# Install dependencies
RUN apt-get install -qqy --no-install-recommends \
        ca-certificates \
        autoconf automake cmake dpkg-dev file git make patch \
        libc-dev libc++-dev libgcc-10-dev libstdc++-10-dev  \
        dirmngr gnupg2 lbzip2 wget xz-utils libtinfo5 g++ && \
    rm -rf /var/lib/apt/lists/*	   	    	      

