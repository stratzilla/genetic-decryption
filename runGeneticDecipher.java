import java.util.Scanner;
import java.io.*;

public class runGeneticDecipher {

	// main entry point for program
	public static void main(String[] args) {
		int maxPop; // population size
		int maxGen; // number of generations
		int crSize; // chromosome size
		int raSeed; // random seed
		int crType; // crossover type
		int muType; // mutation type
		int torNum; // tournament size
		int eltNum; // number of elites
		double crossRate; // crossover rate
		double mutatRate; // mutation rate
		String cryptText; // the encrypted text
		String filename; // for IO later
		try {
			// if invalid number of arguments
			if (args.length != 11) {
				throw new Exception();
			}
			// otherwise get the values from console in
			maxPop = Integer.parseInt(args[0]);
			maxGen = Integer.parseInt(args[1]);
			crSize = Integer.parseInt(args[2]);
			eltNum = Integer.parseInt(args[3]);
			torNum = Integer.parseInt(args[4]);
			crType = Integer.parseInt(args[5]);
			muType = Integer.parseInt(args[6]);
			crossRate = Double.parseDouble(args[7]);
			mutatRate = Double.parseDouble(args[8]);
			raSeed = Integer.parseInt(args[9]);
			filename = (args[10]);
		} catch (Exception e) {
			// remind user how to use
			printHelp();
			return;
		}
		// clamp values if out of range
		raSeed = (raSeed < 1) ? 1 : raSeed;
		crType = (crType < 1) ? 1 : (crType > 4) ? 4 : crType;
		muType = (muType < 1) ? 1 : (muType > 2) ? 2 : muType;
		maxPop = (maxPop < 1) ? 1 : maxPop;
		maxGen = (maxGen < 1) ? 1 : maxGen;
		crSize = (crSize < 1) ? 1 : crSize;
		eltNum = (eltNum < 2) ? 2 : (eltNum > maxPop/2) ? maxPop/2 : eltNum;
		torNum = (torNum < 2) ? 2 : (torNum > 5) ? 5 : torNum;
		crossRate = (crossRate < 0.00) ? 0.00 : (crossRate > 1.00) ? 1.00 : crossRate;
		mutatRate = (mutatRate < 0.00) ? 0.00 : (mutatRate > 1.00) ? 1.00 : mutatRate;
		// initialize encrypted text from file
		cryptText = getEncryptedText(filename);
		if (cryptText.isEmpty()) { printHelp(); return; } // if failed
		// instantiate new GeneticDecipher object with GA parameters
		GeneticDecipher gd = new GeneticDecipher(maxPop, maxGen, crSize, eltNum, torNum, crType, muType, crossRate, mutatRate, raSeed, cryptText);
		// print parameters to console
		printParameters(maxPop, maxGen, crSize, eltNum, torNum, crType, muType, crossRate, mutatRate, raSeed, filename);
		// start the program
		gd.run();
	}
	
	/**
	 * function to load encrypted text from file
	 * @param fn - the filename of the input file
	 * @return - file contents from file inside string
	 */
	private static String getEncryptedText(String fn) {
		String inFile;
		try {
			Scanner sn = new Scanner(new File(fn)).useDelimiter("\\Z");
			inFile = sn.next();
			sn.close();
		} catch (Exception e) {
			return "";
		}
		inFile.toLowerCase(); // make lowercase
		inFile.replaceAll("[^a-z]",""); // remove invalid chars
		inFile.replaceAll("\\s",""); // remove spaces
		inFile.replaceAll("\\n",""); // remove newlines
		return inFile;
	}
	
	/**
	 * function to display GA parameters
	 * @param maxPop - the population size
	 * @param maxGen - the number of generations
	 * @param crSize - the chromosome size
	 * @param eltNum - the number of elites
	 * @param torNum - the tournament size
	 * @param crType - the crossover type
	 * @param muType - the mutation type
	 * @param crossRate - the crossover rate
	 * @param mutatRate - the mutation rate
	 * @param raSeed - the random seed used
	 * @param fn - the filename of encrypted text file
	 */
	private static void printParameters(int maxPop, int maxGen, int crSize, int eltNum, int torNum, int crType, int muType, double crossRate, double mutatRate, int raSeed, String fn) {
		// post to console out the parameters
		System.out.println("\nCurrent GA Parameters:\n");
		System.out.println("Population Size: " + maxPop);
		System.out.println("Number of Generations: " + maxGen);
		System.out.println("Chromosome Size: " + crSize);
		System.out.println("Number of Elites: " + eltNum);
		System.out.println("Tournament Size: " + torNum);
		String crTypeString = "";
		switch (crType) {
			case 1: crTypeString = "1-Point"; break;
			case 2: crTypeString = "2-Point"; break;
			case 3: crTypeString = "Uniform"; break;
			default: crTypeString = "Random"; break;
		}
		String muTypeString = "";
		switch (muType) {
			case 1: muTypeString = "Uniform"; break;
			default: muTypeString = "Non-Uniform"; break;
		}
		System.out.println("Crossover Type: " + crTypeString);
		System.out.println("Mutation Type: " + muTypeString);
		System.out.println("Crossover Rate: " + crossRate);
		System.out.println("Mutation Rate: " + mutatRate);	
		System.out.println("Random Seed: " + raSeed);
		System.out.println("Encrypted Text Used: " + fn + "\n");
	}
	
	/**
	 * function to display compilation instructions if execution failed
	 */
	private static void printHelp() {
		System.out.println("\nCompile and execute the program as so:\n");
		System.out.println(" $ javac runGeneticDecipher.java");
		System.out.println(" $ java runGeneticDecipher <arg1> <arg2> <arg3> <arg4> <arg5> <arg6> <arg7> <arg8> <arg9> <arg10> <arg10> <arg11>\n");
		System.out.println("Where the following are arguments along with their type:\n");
		System.out.println("<arg1> = population size (integer, [1, n])");
		System.out.println("<arg2> = maximum generations (integer, [1, n])");
		System.out.println("<arg3> = chromosome size (integer, [1, n])");
		System.out.println("<arg4> = elite population size (integer, [2, m])");
		System.out.println("         (m < popSize/2)");
		System.out.println("<arg5> = tournament selection k (integer, [2, 5])");
		System.out.println("<arg6> = crossover type (integer, [1, 4])");
		System.out.println("         (1. 1-Point, 2. 2-Point, 3. Uniform, 4. Random)");
		System.out.println("<arg7> = mutation type (integer, [1, 2])");
		System.out.println("         (1. Uniform, 2. Non-Uniform)");		
		System.out.println("<arg8> = crossover rate (double, [0.00, 1.00])");
		System.out.println("<arg9> = mutation rate (double, [0.00, 1.00])");
		System.out.println("<arg10> = random seed (integer, [1, n])");
		System.out.println("<arg11> = filename of input encrypted text (String)\n");
		System.out.println("Any values outside of these ranges will be clamped. 'n' is any maximum.\n");
		System.out.println("The last argument, <arg11>, is the filename to a text file containing encrypted text.\n");
	}
}
