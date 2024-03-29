## Bigram Count and Map Task Count Using Hadoop MapReduce

### Written by [@malec](https://github.com/malec)

### Build and run:

```javac BigramCount.java && jar -cvf BigramCount.jar ./BigramCount*.class && rm -rf output && /usr/local/hadoop/bin/hadoop jar BigramCount.jar BigramCount input output```

### Submission package script:
```tar -cvf pa2_Ahlbrandt.tar input output BigramCount.java answer.txt```

### Determining the number of bigrams in a set of input files
I look at every pair of words delimited by " \t\n\r\f" (the default delimiter set for the java StringTokenizer class). [More information on bigrams](https://en.wikipedia.org/wiki/Bigram). In the map stage, a pair of words are combined in the format '{previous word} {next word}'. For each line, only pairs of words (bigrams) will qualify as a key - not a single word. For each key, an IntWritable with value of one is emitted. After the shuffle and sort phase is complete, the key and values of the bigrams are written to reducer context, and written to the result file.

### Counting the number of map tasks
New to the reducer class is a cleanup method. This is executed after all mapping is complete, and is used to count the number of instantiated map tasks. When mapping is finished, in the cleanup method, a key-value of A-Map-Task-Count and 1 is written to the context. This pair will be reduced to a value of 6.
