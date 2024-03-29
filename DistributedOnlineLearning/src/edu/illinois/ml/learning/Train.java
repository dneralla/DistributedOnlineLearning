package edu.illinois.ml.learning;
import java.io.IOException;
import java.util.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Train extends Configured implements Tool {

	//Usage
	static final String USAGE = "Train -i <input_folder> -o <output_folder> [options]";

	//Keys to find HadoopPerceptron options in the configuration
	static final String K_HAS_INPUT_PARAMS="HP.has.input.params"; //TODO? useless?
	static final String K_PARAMETERS_FOLDER="HP.parameters.folder";
	static final String K_INPUT_FOLDER="HP.input.folder";
	static final String K_OUTPUT_FOLDER="HP.output.folder";
	static final String K_N_MAP="HP.number.map.tasks";
	static final String K_N_REDUCE="HP.number.reduce.tasks";
	static final String K_N_SENTENCE_ITERATIONS="HP.number.sentence.iterations";
	static final String K_PERCEPTRON_TYPE="HP.perceptron.type";
	static final String K_SLACK="HP.passive.aggressive.slack.variable";

	//Defaul values for options
	static final int D_N=1;
	static final int D_S=1;
	static final float D_C=2;
	static final String D_P="P";
	
	//Range of values for options
	static final String V_P="P|AV|PA|PA1|PA2";
	
	static Options options=initOptions();
	private static Options initOptions(){
		Options options = new Options();

		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("Display usage.");
		options.addOption(OptionBuilder.create("help"));

		OptionBuilder.withArgName("input_folder");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Folder in the hadoop dfs containing the training corpus.");
		OptionBuilder.isRequired(true);
		options.addOption(OptionBuilder.create("i"));

		OptionBuilder.withArgName("output_folder_prefix");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Prefix of the name for the folder in the hadoop dfs where the model parameters are going to be saved.");
		OptionBuilder.isRequired(true);
		options.addOption(OptionBuilder.create("o"));

		OptionBuilder.withArgName("integer");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Number of times the training is repeated on each sentence by a map worker. default value is "+D_S+".");
		OptionBuilder.withType(Integer.class);
		options.addOption(OptionBuilder.create("S"));

		OptionBuilder.withArgName("integer");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Number of Parameter Mixing iterations. default value is "+D_N+".");
		OptionBuilder.withType(Integer.class);
		options.addOption(OptionBuilder.create("N"));

		OptionBuilder.withArgName("parameters_folder");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Folder in the hadoop dfs containing the parameters used to initialize the model.");
		options.addOption(OptionBuilder.create("p"));

		OptionBuilder.withArgName("integer");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Set recommended number of map tasks.");
		OptionBuilder.withType(Integer.class);
		options.addOption(OptionBuilder.create("M"));

		OptionBuilder.withArgName("integer");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Set recommended number of reduce tasks.");
		OptionBuilder.withType(Integer.class);
		options.addOption(OptionBuilder.create("R"));

		OptionBuilder.withArgName("("+V_P+")");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Set perceptron parameters update technique. Possible values are:" +
				"\n\tP:  standard perceptron update." +
				"\n\tAV: averaged perceptron"+
				"\n\tPA: passive-aggressive perceptron update." +
				"\n\tPA1: passive-aggressive 1 (with slack variable). use parametere -C to set the slack variable." +
				"\n\tPA2: passive-aggressive 2 (with quadratic slack variable). use parametere -C to set the slack variable." +
				"\n default value is "+D_P+".");
		OptionBuilder.withType(Integer.class);
		options.addOption(OptionBuilder.create("P"));
		
		OptionBuilder.withArgName("float");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Set slack variable for PA1 and PA2. default value is "+D_C+".");
		OptionBuilder.withType(Double.class);
		options.addOption(OptionBuilder.create("C"));

		return options;
	}

	public static class Map extends MapReduceBase implements
	Mapper<LongWritable, Text, Text, DoubleWritable> {

		private Perceptron perceptron;
		JobConf conf;

		@Override
		public void configure(JobConf jc) {
			conf = jc;
			
			String pType = conf.get(K_PERCEPTRON_TYPE);
			if(pType==null || pType.equals("P"))	perceptron = new PerceptronStandard();
//			else if(pType.equals("AV"))perceptron = new PerceptronAveraged();
//			else if(pType.equals("PA"))perceptron = new PerceptronPassiveAggressive();
//			else if(pType.equals("PA1"))perceptron = new PerceptronPassiveAggressive1(conf.getFloat(K_SLACK,D_C));
//			else if(pType.equals("PA2"))perceptron = new PerceptronPassiveAggressive2(conf.getFloat(K_SLACK,D_C));
			
			if (conf.getBoolean(K_HAS_INPUT_PARAMS, false))
				perceptron.readWeights(conf);
		}

		public void map(LongWritable key, Text value,
				OutputCollector<Text, DoubleWritable> output, Reporter reporter)
						throws IOException {

			//Sentence sentence = new Sentence(value.toString());
			//String prevPredLabel;
			try {
				perceptron.train(FeatureExtractor.getFeatures(value.toString()));
			} catch (Exception e) {
			   e.printStackTrace();
			}
           perceptron.collectOutput(output);
		}
	}

	public static class Reduce extends MapReduceBase implements
	Reducer<Text, DoubleWritable, Text, DoubleWritable> {
		JobConf conf = null;

		@Override
		public void configure(JobConf jc) {
			conf = jc;
		}

		public void reduce(Text key, Iterator<DoubleWritable> values,
				OutputCollector<Text, DoubleWritable> output, Reporter reporter)
						throws IOException {
			double sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			DoubleWritable weight = new DoubleWritable(sum);
			output.collect(key, weight);
		}
	}

	public int run(String[] args) throws Exception {

		try{
			JobConf conf = new JobConf(getConf(), Train.class);
			conf.setJobName("train");

			conf.setOutputKeyClass(Text.class);
			conf.setOutputValueClass(DoubleWritable.class);

			conf.setMapperClass(Map.class);
			conf.setCombinerClass(Reduce.class);
			conf.setReducerClass(Reduce.class);

			conf.setInputFormat(TextInputFormat.class);
			conf.setOutputFormat(TextOutputFormat.class);

			FileInputFormat.setInputPaths(conf, new Path(conf.get(K_INPUT_FOLDER)));
			FileOutputFormat.setOutputPath(conf, new Path(conf.get(K_OUTPUT_FOLDER)));

			int nMap=conf.getInt(K_N_MAP,-1);
			if (nMap>0){
				conf.setNumMapTasks(nMap);
			}
			int nRed=conf.getInt(K_N_REDUCE,-1);
			if (nRed>0){
				conf.setNumReduceTasks(nRed);
			}

			String paramFolder=conf.get(K_PARAMETERS_FOLDER);
			if (paramFolder!=null) {// init params are specified
				if(DistributedCacheUtils.loadParametersFolder(paramFolder, conf)==1)return 1;
				conf.setBoolean(K_HAS_INPUT_PARAMS, true);
			}

			JobClient.runJob(conf);

			return 0;
		}catch (FileAlreadyExistsException e){
			System.err.println("\nError:\n"+e.getMessage()+"\n");
			return 1;
		}
	}


	public static void main(String[] args) throws Exception {
		if(Arrays.asList(args).contains("-help")){
			new HelpFormatter().printHelp( USAGE, options );
			System.exit(0);
		}
		try{
			CommandLine cmd = new PosixParser().parse(options, args);

			int	numIterations= Integer.parseInt(cmd.getOptionValue("N",""+D_N));
			String inputDir = cmd.getOptionValue("i");
			String outputDirPref = cmd.getOptionValue("o");

			Configuration invariantConf= new Configuration();
			invariantConf.set(K_INPUT_FOLDER, inputDir);
			invariantConf.set(K_N_SENTENCE_ITERATIONS,cmd.getOptionValue("S",""+D_S));
			if (cmd.hasOption( "M" )) invariantConf.set(K_N_MAP,cmd.getOptionValue("M"));
			if (cmd.hasOption( "R" )) invariantConf.set(K_N_REDUCE,cmd.getOptionValue("R"));
			if (cmd.hasOption( "C" )) invariantConf.set(K_SLACK,cmd.getOptionValue("C"));
			if (cmd.hasOption( "p" )) invariantConf.set(K_PARAMETERS_FOLDER, cmd.getOptionValue("p")); //this is going to be overwritten for iterations different from the first
			if (cmd.hasOption( "P" )){
				if(!Arrays.asList(V_P.split("\\|")).contains(cmd.getOptionValue("P")))throw new ParseException("The allowed values for option -P are: ("+V_P+")");
				invariantConf.set(K_PERCEPTRON_TYPE, cmd.getOptionValue("P"));
			}else{
				invariantConf.set(K_PERCEPTRON_TYPE,D_P);
			}
				
			Configuration conf;
			for (int i = 0; i < numIterations; i++) {
				conf= new Configuration(invariantConf);

				conf.set(K_OUTPUT_FOLDER, outputDirPref+"_"+(i+1));
				if (i>0)conf.set(K_PARAMETERS_FOLDER, outputDirPref+"_"+i);

				System.out.println("\n====================\nPARAMETER MIXING ITERATION: " + (i + 1));
				if( ToolRunner.run(conf, new Train(), new String[0]) ==1) System.exit(1);//arguments are passed via the configuration
			}
			System.exit(0);
		}

		catch( ParseException e ) {
			new HelpFormatter().printHelp( USAGE, options );
			System.err.println("\n\nError while parsing command line:\n"+e.getMessage()+"\n");
		}
		catch( NumberFormatException e ) {
			new HelpFormatter().printHelp( USAGE, options );
			System.err.println("\n\nError while parsing command line:\n"+e.getMessage()+"\n");
		}
	}

}
