LOC       6
Data   10
Data 3
Data END
Data 0
Data      12
Data 9
Data 18
Data 12
LDX 2,7
LDR    3,0,10
LDR 2,2,10
LDR 1,2,10,1
LDA      0,0,0
LDX 1,8
JZ 0,1,0
LOC 1024
END: HLT
STR 3,0,9
STX 3,8
JNE 2,0,15
JCC 1,0,12
JMA 2,15
JSR 2,15
RFS 33
SOB 1,0,5
JGE 2,0,10
AMR 3,2,8
SMR 1,2,8,1
AIR 2,7
SIR 2,3
MLT 0,2
DVD 0,2
TRR 0,3
AND 1,3
ORR 2,3
NOT 3
RRC 0,2,1,0
SRC 0,2,1,0