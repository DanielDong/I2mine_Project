Four inputs for now:

processure sequence for now is p1, p2, p3, p4

All in .xls format
==========================

1) workface-distance:
This file contains a NxN matrix holding distances between all workphases.
Each cell contains the exact distance in meters between two workphases.
e.g. 
   w1  w2  w3                                   A1  B1  C1
w1 ..  ..  ..                                1  ..  ..  ..
w2 ..  ..  ..                                2  ..  ..  ..
w3 ..  ..  ..   distance matrix is stored in 3  ..  ..  ..
(.. means distance value)


2) workface-state:
This file indicates whether each workphase is available or not.
Each cell only has two possible values (1 availabe, 0 not available).
Column A1, B1, C1... indicates workphase 1, workphase 2, workphase 3...

3)machine-op-info:
WHAT IS THE OR unit and MR unit, also P1, P2, P3...???
both units are in meter/min


4)workface-workload:
WHAT IS THE UNIT???
workload is measured by meter/min