mpi-network-motif
==================

Network Motif analysis using MPI (Java)

## Test Drive

#### Prerequisites

- Mac/Linux platform
- `labelg` program compiled for your computer. If you're working through the UWB Linux Lab, the provided executable will suffice. Otherwise you will need to compile and include your own: replace `labelg` with an identically named executable and change the mode to allow execution (`chmod +x labelg`). The source is available at http://cs.anu.edu.au/~bdm/nauty/ (to build, `bash configure && make all`).
- mpiJava (http://www.hpjava.org/mpiJava.html).
- MPI.

#### Setup

- Configure the **mpiJava class path** in the script `run.sh` (preconfigured for UWB Linux Lab).
- Create and configure the contents of `.mpd.conf` to be `secretword=your-secret-word`, where `your-secret-word` is a secure key that only you know.
- Create and configure `mpd.hosts` to be the remotes nodes you wish to add to the MPI network at execution time.


```
Project structure:

.
├── run.sh               <- configure
├── .mpd.conf            <- create
├── mpd.hosts            <- create
├── labelg               <- provided for UWB linux lab
├── README.md            (this file)
├── data/                (data files)
├── src/                 (source code)
│   ├── Main.java
│   └── ...
└── ...                  (other files)
```

#### Execution

Compile the code, then run the program on the master node:
``` bash
$ ./compile.sh

$ ./run.sh datafile motif nThreads [--show-results]

# examples:

# motif size of 3, 4 threads per node, print canonical label counts:
$ ./run.sh data/Scere20131031CR.txt 3 4 --show-results

# motif size of 4, 2 threads per node, don't print results
$ ./run.sh data/test05 3 2
```

## Measuring Performance

For convenience, this program includes a script to automate testing program performance for a variety of scenarios. `measure.sh` assumes that the `run.sh` script is properly configured (as explained above). This script runs the program in the following scenarios: 1, 2, 4, 8, and 16 nodes, each at 1, 2, and 4 threads per node, for a total of 15 different combinations. This script is designed for the UWB Linux Lab.

#### Execution

``` bash
$ ./measure.sh datafile motif

# example:
# run all scenarios on test04 file, with motif size 6
$ ./measure.sh data/test04 6
```

## Data File Format

Text file, with each line containing a "from" node and a "to" node, as indicated by the string. These are interpreted to be undirected connections, so the ordering does not actually matter.

Example:

```
a b
b c
b d
d e
```

This represents the following graph:

```
 ___        ___        ___
| a | ---- | b | ---- | c |
 ---        ---        ---
             |
             |
            ___        ___
           | d | ---- | e |
            ---        ---
```
