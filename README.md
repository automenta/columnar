# big dataframes 

Kotlin Blackboard contexts for composable operations on composable data IO features. 
this is purpose-built early implementations for large scale time series LSTM dataprep  

  - [X] read an FWF text and efficiently mmap the row access (becomes a cursors iterator) `*`
  - [X] enable index operations, reordering, expansions, preserving column metadata 
  - [X] resample timeseries data (jvm LocalDate initially) to fill in series gaps
  - [X] concatenation of n cursors from disimilar FP projections
  - [X] pivot n rows by m columns (lazy) preserving l left-hand-side pass-thru columns
  - [X] groupby n columns
  - [X] cursor.group(n..){reducer} 
  - [ ] One-hot Encodings 
  - [ ] min/max scaling (same premise as resampling above)
  - [ ] support Numerics, Linear Algebra libraries
  - [X] support for (resampling) Calendar, Time and Units conversion libraries
  - [X] orthogonal offheap and indirect IO component taxonomy
  - [X] nearly 0-copy direct access
  - [X] nearly 0-heap direct access
  - [X] large file access: JVM NIO mmap window addressability beyond MAXINT bytes   
  - [X] Algebraic Vector aggregate operations with lazy runtime execution on contents
  - [ ] Mapper Buffer pools 
 
### lower priorities (as-yet unimplemented orthogonals)
 - [X] a token amount of jvm switch testing.
 - [X] textual field format IO/mapping
 - [X] binary  field format IO/mapping (network endian binary int/long/ieee)
 - [ ] json    field format IO/mapping
 - [ ] CBOR    field format IO/mapping
 - [ ] csv IO tokenization +- headers
 - [ ] gossip mesh addressable cursor iterators
 - [ ] columnstore access patterns +- apache arrow compatibility
 - [ ] matrix math integrations with adjacent ecosystem libraries
 - [ ] key-value associative cursor indexes
 - [ ] hilbert curve iterators for converting (optimal/bad) cache affinity patterns to (good/good) cache affinity
 - [ ] R-Tree n-dimensional associative
 - [ ] parallel and concurrent access helpers
 - [ ] explicit platter and direct partition mapping
 - [ ] jdbc adapters `*`
 - [ ] sql query language driver `*`
 - [ ] jq query language driver
 
 `*` downstream of [jdbc2json](https://github.com/jnorthrup/jdbc2json)
 
Figure below: Orthogonal Context elements (Sealed Class Hierarchies).
   
These describe different aspects of accessing 
data and projecting columnar and matrix transformations 
These are easy to think of as hierarchical threadlocals to achieve IOBound storage access to large datasets. 


![image](https://user-images.githubusercontent.com/73514/71553240-7a838500-2a3e-11ea-8e3e-b85c0602873f.png)

inspired by the [STXXL](https://stxxl.org)  project


## jvm switches

this code is extremly tuning sensitive to jvm opts.

running multiple threads might make sense on multiple storage volumes but is not showing immediate gains in mappping in FWF mmap content of sub-MAXINT bytes. presumably, one IO thread is adequate to keep the other CPU cores buysy collecting the currently un-pooled garbage.
.

so far for single-threaded benchmarking of 500k records in 225MB fwf/text file the basic parameters each seem to lead toward a better thruput

`-Xms24G -Xmx24G -XX:MaxDirectMemorySize=1G -XX:-PrintCompilation -XX:+UseTransparentHugePages -XX:MaxBCEAEstimateSize=2m`

  * `-Xms24G -Xmx24G` large heap and preallocatation is simply a linear increase in gc headroom. 
  *  `-XX:MaxDirectMemorySize=1G` Directmemory should be in the ballpark of large files if convenient.     
  *  `-XX:+UseTransparentHugePages ` tends to add allocator options helpful for larger memory tasks.   
  *  `-XX:MaxBCEAEstimateSize=2m` influences the choice of escape analysis to increase the available cycles for IO/FWF
   mapping that is dominated by excessive GC.  kotlin by itself has coroutine methods as large as 380 bytes in this codebase