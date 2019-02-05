import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import java.util.*;

public class BigramCount {
	public static class Tuple<K,V> implements Comparable<Tuple>, Writable {
		public K x;
		public V y;

		public Tuple() {

		}

		public Tuple(K x, V y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Tuple o) {
			if (x.toString() == o.x.toString() && ((IntWritable)this.y).get() == ((IntWritable)o.y).get()) {
				return 0;
			}
			else return -1;
		}

	}

	public static class TokenizerMapper extends Mapper<Object, Text, Text, Tuple<Text, IntWritable>> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			if (itr.countTokens() != 1) {
				Text previous = new Text(itr.nextToken());
				while (itr.hasMoreTokens() && itr.countTokens() != 1) {
					word.set(previous);
					Text next = new Text(itr.nextToken());
					context.write(previous, new Tuple(next, one));
					previous = next;
				}
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, Tuple, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: BigramCount <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(BigramCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Tuple.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
