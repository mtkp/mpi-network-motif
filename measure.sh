#!/bin/bash -e

# usage: measure.sh data_file motif_size
#  note this script assumes execution from uw1-320-00

data_file=$1
motif_size=$2

# make backup
if [ -e mpd.hosts.tmp ] ; then
  cp mpd.hosts.tmp mpd.hosts
fi
cp mpd.hosts mpd.hosts.tmp

# run the program with 1, 2, and 4 threads
run_prog() {
  ./run.sh 2>&1
}

# 1 node
echo -n "" > mpd.hosts
run_prog

# 2 nodes
echo "uw1-320-01" > mpd.hosts
run_prog

# 4 nodes
echo "uw1-320-01
uw1-320-02
uw1-320-03" >  mpd.hosts
run_prog

# 8 nodes
echo "uw1-320-01
uw1-320-02
uw1-320-03
uw1-320-04
uw1-320-05
uw1-320-06
uw1-320-07" > mpd.hosts
run_prog

# 16 nodes
echo "uw1-320-01
uw1-320-02
uw1-320-03
uw1-320-04
uw1-320-05
uw1-320-06
uw1-320-07
uw1-320-08
uw1-320-09
uw1-320-10
uw1-320-11
uw1-320-12
uw1-320-13
uw1-320-14
uw1-320-15" > mpd.hosts
run_prog

mv mpd.hosts.tmp mpd.hosts
