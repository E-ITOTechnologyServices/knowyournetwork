### CMake for nfelastic ###

cmake_minimum_required(VERSION 2.8)

#
# This cmake project file defines how to build tools 
# "nfcapd" and "nfdump" from nfelastic sources
# (modified nfcapd sources)
#
# Usage:
# 1. create an empty build directory outside source tree: 
#	 $ mkdir build; cd build
# 2. execute cmake from within build directory, argument is source directory
#	 $ cmake ../nfelastic
# 2a. for use with Eclipse IDE
#    $ cmake -G"Eclipse CDT4 - Unix Makefiles" ../nfelastic
#       In Eclipse:
#       - Import project using Menu File->Import
#       - Select General->Existing projects into workspace:
#       - Browse and select the build directory. Keep "Copy projects into workspace" unchecked.
#       - You get a fully functional eclipse project.
#       - You may rename project, but it should never have the same name as the build directory.
# 3. execute make inside build dir
#	 $ make
#
# Remark:
# To compile on optimized version, execute cmake with parameter "-DCMAKE_BUILD_TYPE=Release"
#


#
### add common definitions here
#
add_definitions(-DHAVE_CONFIG)


#
### define executable target "nfcapd"
#
add_executable(nfcapd
	bin/nfcapd.c
	bin/nf_common.c 
	bin/util.c 
	bin/minilzo.c 
	bin/nffile.c 
	bin/nfx.c 
	bin/nfxstat.c 
	bin/flist.c 
	bin/fts_compat.c 
	bin/nfstatfile.c 
	bin/launch.c 
	bin/nfnet.c 
	bin/collector.c 
	bin/netflow_v1.c 
	bin/netflow_v5_v7.c 
	bin/netflow_v9.c 
	bin/ipfix.c 
	bin/bookkeeper.c 
	bin/expire.c 
	bin/elastic.c
	)

#
# define include directories
#
target_include_directories(nfcapd PUBLIC bin/ 	)
target_include_directories(nfcapd PUBLIC generated/ )
#
# define needed libraries to link to
#
target_link_libraries(nfcapd LINK_PUBLIC resolv curl jansson pthread)


install(TARGETS nfcapd DESTINATION bin)

#
### define executable target "nfdump"
#
add_executable(nfdump
	bin/nfdump.c	
	bin/nfstat.c
	bin/nfexport.c
	bin/nf_common.c
	bin/nflowcache.c
	bin/util.c
	bin/minilzo.c 
	bin/nffile.c 
	bin/nfx.c 
	bin/nfxstat.c 
	bin/flist.c 
	bin/fts_compat.c 
	generated/grammar.c 
	generated/scanner.c 
	bin/nftree.c 
	bin/ipconv.c 
	bin/nfprof.c 
	bin/exporter.c
	bin/elastic.c
	)

#
# define include directories
#
target_include_directories(nfdump PUBLIC bin/ )
target_include_directories(nfdump PUBLIC generated/ )
#
# define needed libraries to link to
#
target_link_libraries(nfdump LINK_PUBLIC resolv curl jansson pthread)

install(TARGETS nfdump DESTINATION bin)
