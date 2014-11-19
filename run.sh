#!/bin/bash -e

# usage
( test -z $3 ) &&
  echo "usage: run.sh data_file motif_size threads_per_node [--show-results]" &&
  echo "       (default is to not show results)" &&
  echo "  ex1: run.sh path/to/data 3 4 --show-results" &&
  echo "  ex2: run.sh path/to/data 5 2" &&
  exit 1

# terminate any existing MASS nodes
mpiexec -n 1 /bin/hostname 2>&1 1>/dev/null && mpdallexit
sleep 1

# execution setup
program="Driver"
data_file=$1
motif_size=$2
threads_per_node=$3
show_results=$4
mpi_cp="/usr/apps/mpiJava-`uname -p`/lib/classes"
cp_additions="build"
nodes=$((`cat mpd.hosts | wc -w` + 1))

mpdboot -n $nodes
mpirun -n $nodes java -cp $mpi_cp:$cp_additions:. Driver \
  $nodes \
  $threads_per_node \
  $data_file \
  $motif_size \
  $show_results
mpdallexit
