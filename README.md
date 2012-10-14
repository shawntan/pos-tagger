Part-of-Speech Tagger
=====================

U096883L Shawn Tan

1. `Tagger.java`: Main part of the program. Contains code for:
	# Learning transition probabilities from a given text file
	# Viterbi algorithm for inferring the POS tags for a string
	# Smoothing method is modular, and implemented by extending (and then adding) the `Smoother` inner class.

2. `build_tagger.java` : Implements codes that instantiates `Tagger` and initiates learning.
	On running `java build_tagger train_file test_file model_file`, the following occurs:
    1. Instantiates `Tagger` and initialises learning using `train_file`.
    2. Model is written to `model_file` as a serialised object.
    3. Proceeds to evaluate learnt model using `test_file`. `tag_file` has to be a labelled file in the same format as `train_file`
    4. Outputs confusion matrix for the test: a (No. of POS tags) X (No. of POS tags) grid.
    5. Outputs Recall, Precision and F1-measures per tag.
3. `run_tagger.java` : Usage `java run_tagger test_file model_file out_file`
	On running, unserializes model from `model_file` and tags sentences in `test_file` before outputting them on `out_file`	
