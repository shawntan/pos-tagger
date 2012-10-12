#!/bin/bash

for i in $(seq 10) 
do
	echo "Evaluating part $i..."
	java -cp bin build_tagger "a2_data/sents.train"$i"_trn" "a2_data/sents.train"$i"_tst" model_file > report_$i
done
