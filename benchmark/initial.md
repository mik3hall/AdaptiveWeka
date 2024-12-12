Even if the classifier itself doesn't support parallel, if cross validation does you still get a performance gain. I included SMO benchmarks against the same dataset for this. 

*** RandomForest *** \
*** CV Fold 3 \
Default Adaptive(3 fold): used 2 threads. Elapsed(sec) 8.10. accuracy 94.75 \
Single Thread for fold 3 Elapsed(sec) 21.00. accuracy 94.75 \
&nbsp;&nbsp;&nbsp;&nbsp;2 7817 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 4497 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 4497 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 4486 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 4482 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 4521 used 7 \
Slotted: \
Avg: 6809	Min: 4539 Thread 7.	Max: 0 Thread 0 \
Adaptive: \
Avg: 5050	Min: 4482 Thread 6.	Max: 4521 Thread 7 \
*** CV Fold 5 \
Default Adaptive(5 fold): used 3 threads. Elapsed(sec) 10.12. accuracy 94.83 \
Single Thread for fold 5 Elapsed(sec) 45.03. accuracy 94.83 \
&nbsp;&nbsp;&nbsp;&nbsp;2 14292 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 10113 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 10577 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 6586 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 6577 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 6631 used 7 \
Slotted: \
Avg: 13404	Min: 8676 Thread 7.	Max: 0 Thread 0 \
Adaptive: \
Avg: 9129	Min: 6577 Thread 6.	Max: 10577 Thread 4 \
*** CV Fold 8 \
Default Adaptive(8 fold): used 4 threads. Elapsed(sec) 17.32. accuracy 94.97 \
Single Thread for fold 8 Elapsed(sec) 79.11. accuracy 94.98 \
&nbsp;&nbsp;&nbsp;&nbsp;2 21015 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 16503 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 17314 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 17388 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 18205 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 22030 used 7 \
Slotted: \
Avg: 23176	Min: 14825 Thread 7.	Max: 0 Thread 0 \
Adaptive: \
Avg: 18742	Min: 16503 Thread 3.	Max: 22030 Thread 7 \
*** CV Fold 10 \
Default Adaptive(10 fold): used 5 threads. Elapsed(sec) 18.72. accuracy 95.40 \
Single Thread for fold 10 Elapsed(sec) 101.98. accuracy 95.29 \
&nbsp;&nbsp;&nbsp;&nbsp;2 27058 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 22415 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 18748 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 19078 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 19244 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 23318 used 7 \
Slotted: \
Avg: 29710	Min: 18957 Thread 7.	Max: 0 Thread 0 \
Adaptive: \
Avg: 21643	Min: 18748 Thread 4.	Max: 23318 Thread 7 \
*** SMO *** \
*** CV Fold 3 \
Default Adaptive(3 fold): used 2 threads. Elapsed(sec) 58.45. accuracy 93.15 \
Single Thread for fold 3 Elapsed(sec) 86.73. accuracy 93.15 \
&nbsp;&nbsp;&nbsp;&nbsp;2 58299 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 30525 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 30657 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 30648 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 30581 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 30507 used 7 \
Adaptive: \
Avg: 35202	Min: 30507 Thread 7.	Max: 30657 Thread 4 \
*** CV Fold 5 \
Default Adaptive(5 fold): used 3 threads. Elapsed(sec) 55.42. accuracy 93.54 \
Single Thread for fold 5 Elapsed(sec) 133.76. accuracy 93.54 \
&nbsp;&nbsp;&nbsp;&nbsp;2 81776 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 55612 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 54998 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 30104 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 30189 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 31054 used 7 \
Adaptive: \
Avg: 47288	Min: 30104 Thread 5.	Max: 31054 Thread 7 \
*** CV Fold 8 \
Default Adaptive(8 fold): used 4 threads. Elapsed(sec) 56.52. accuracy 93.48 \
Single Thread for fold 8 Elapsed(sec) 208.26. accuracy 93.48 \
&nbsp;&nbsp;&nbsp;&nbsp;2 106344 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 81403 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 56089 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 56417 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 57765 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 58590 used 7 \
Adaptive: \
Avg: 69434	Min: 56089 Thread 4.	Max: 58590 Thread 7 \
*** CV Fold 10 \
Default Adaptive(10 fold): used 5 threads. Elapsed(sec) 57.87. accuracy 93.55 \
Single Thread for fold 10 Elapsed(sec) 263.84. accuracy 93.54 \
&nbsp;&nbsp;&nbsp;&nbsp;2 135575 used 2 \
&nbsp;&nbsp;&nbsp;&nbsp;3 109249 used 3 \
&nbsp;&nbsp;&nbsp;&nbsp;4 83090 used 4 \
&nbsp;&nbsp;&nbsp;&nbsp;5 58253 used 5 \
&nbsp;&nbsp;&nbsp;&nbsp;6 59311 used 6 \
&nbsp;&nbsp;&nbsp;&nbsp;7 61281 used 7 \
Adaptive: \
Avg: 84459	Min: 58253 Thread 5.	Max: 61281 Thread 7
