#!/bin/bash -e

# usage
#( test -z $2 ) &&
#  echo "usage: run.sh data_file motif_size threads [--show-results]" &&
#  echo "       (default is to not show results)" &&
#  echo "  ex1: run.sh path/to/data 3 4 --show-results" &&
#  echo "  ex2: run.sh path/to/data 5 2" &&
#  exit 1

# execution variables
program="Driver"

# terminate any existing MASS nodes
#mpdallexit
sleep 1

# execution setup
#data_file=$1
#motif_size=$2
#threads_per_node=$3
#show_results=$4
mpi_cp="/usr/apps/mpiJava-`uname -p`/lib/classes"
cp_additions="build"
nodes=$((`cat mpd.hosts | wc -w` + 1))

mpdboot -n $nodes -v
mpirun -n $nodes java -cp $mpi_cp:$cp_additions:. Driver
mpdallexit
