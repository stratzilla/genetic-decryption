import static java.lang.Math.random;
import static java.lang.Math.floor;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;

public class GeneticDecipher {

	// encrypted text
	private final String encText;
	// valid characters for chromosome
	private static String OPTIONS = "abcdefghijklmnopqrstuvwxyz-";
	// GA parameters user defined
	private final int maxPop;
	private final int maxGen;
	private final int crSize;
	private final int raSeed;
	private int crType;
	private int muType;
	private final int torNum;
	private final int eltNum;
	private final double crossRate;
	private double mutatRate;
	private final double initialMutatRate;
	// RNG
	private final Random rnd;
	// Collection of chromosomes
	private ArrayList<KeyScorePair> chrColl = new ArrayList<KeyScorePair>();
	// for elites
	private ArrayList<KeyScorePair> eltColl = new ArrayList<KeyScorePair>();
	// for tournamenting
	private ArrayList<KeyScorePair> tourn = new ArrayList<KeyScorePair>();
	
	// constructor for class
	public GeneticDecipher(int mp, int mg, int cs, int en, int tn, int ct, int mt, double cr, double mr, int rs, String ec) {
		// initialize attributes from parameters
		maxPop = mp; maxGen = mg; crSize = cs; crType = ct; torNum = tn;
		raSeed = rs; crossRate = cr; mutatRate = mr; eltNum = en; muType = mt;
		encText = ec;
		// initial mutation rate (meant for non-uniform mutation later)
		initialMutatRate = mr;
		// initialize with seed
		rnd = new Random(raSeed);
	}
	
	/**
	 * the primary GA method
	 * the general scheme of the GA is as follows:
	 * - generate random population
	 * - for each generation 0..maxGen
	 * 	- sort by fitness
	 *	- find some number of elites to transfer verbatim to next gen
	 *	- tournament of k random chromosomes, one winner chosen
	 *	- erase rest of population
	 *	- between tournament winner and elites select two to make 90% offspring
	 *		(randomly two elites or randomly one elite and tournament winner)
	 *	- perform crossover (1P, 2P, or UF) and mutation (uniform or non-uniform)
	 *		based on crossover rate and mutation rate
	 *	- the other 10% of the population is random/novel
	 * - endfor
	 */
	public void run() {
		// Initial Population Initializer
		initializePopulation();
		// for each generation
		for (int i = 0; i < maxGen+1; i++) {
			// sort by fitness
			chrColl.sort(Comparator.comparing(KeyScorePair::getScore));
			// prematurely end if solved
			if (chrColl.get(0).getScore() == 0.00) { break;	}
			// find elites
			findElites();
			// find tournament candidates
			findTournament();
			// output generation number and best fit/chromosome
			bestInGeneration(eltColl.get(0), i);
			// we don't need the rest of the population anymore
			chrColl.clear();
			// Repopulate from tournament
			repopulate();
			// erase for new tournament and elites
			tourn.clear();
			eltColl.clear();
			// if mutation is not uniform, i.e. changing with time
			if (muType == 2 /** && i > (maxGen/3) */ ) {
				// find graduations between initial..1.00 based on gens
				double steps = (1-initialMutatRate)/(double)maxGen;
				mutatRate += steps; // slightly increase mutation
				mutatRate = (mutatRate > 1.00) ? 1.00 : mutatRate; // but keep max
			}
		}
		// output best in run
		bestInRun(chrColl.get(0));
	}
	
	/**
	 * elite selection method
	 */
	private void findElites() {
		// until we have enough
		for (int i = 0; eltColl.size() != eltNum && i < chrColl.size(); i++) {
			// we want unique elites
			if (eltColl.contains(chrColl.get(i))) { continue; }
			eltColl.add(chrColl.remove(i));
		}
	}
	
	/**
	 * method to find tournament candidates
	 */
	private void findTournament() {
		int index;
		for (int i = 0; i < torNum; i++) {
			/**
			 * a tournament size of k = user defined is used; select
			 * k random chromosomes from the current generation
			 */
			index = rnd.nextInt(chrColl.size()-2)+2;
			tourn.add(chrColl.remove(index));
		}
		// sort them since we want the winner later
		tourn.sort(Comparator.comparing(KeyScorePair::getScore));
	}
	
	/**
	 * method to repopulate population
	 */
	private void repopulate() {
		/**
		 * elitism ensures the population doesn't fluctuate
		 * and instead always goes up (or remains the same)
		 * consider two parent elites and transfer to the next
		 * generation verbatim
		 */
		for (int i = 0; i < eltColl.size(); i++) {
			chrColl.add(eltColl.get(i));
		}
		// number of elites .. maxPop*9/10
		for (int i = eltColl.size(); i < 9*(maxPop/10); i+=2) { // for each population / 2 (two children each run)
			String p1, p2;
			if (rnd.nextInt(2) == 1) { // use two elites
				/**
				 * we want to use two different elites, so we need
				 * a temp placeholder to hold it when we remove it
				 * so we can add it back later
				 */
				KeyScorePair temp = eltColl.remove(rnd.nextInt(eltColl.size()));
				// first elite at random
				p1 = temp.getKey();
				// second elite at random
				p2 = eltColl.get(rnd.nextInt(eltColl.size())).getKey();
				// add it back
				eltColl.add(temp);
			} else { // use one elite and tournament winnter
				// tournament winner
				p1 = tourn.get(0).getKey();
				// one elite at random
				p2 = eltColl.get(rnd.nextInt(eltColl.size())).getKey();
			}
			// make two children from crossovered parents
			String children[] = crossChromosome(p1, p2);
			// apply mutation and add
			chrColl.add(new KeyScorePair(mutateChromosome(children[0]), encText));
			chrColl.add(new KeyScorePair(mutateChromosome(children[1]), encText));
		}
		// let the bottom 10% become novel chromosomes for diversity
		for (int j = 9*(maxPop/10); j < maxPop; j++) {
			String chr = getRandChromosome(crSize);
			chrColl.add(new KeyScorePair(chr, encText));
		}
	}
	
	/**
	 * method to perform crossover on chromosome
	 * available crossover techniques are 1-Point, 2-Point, and Uniform
	 * @param m - the first parent
	 * @param f - the second parent
	 * @return - a crossovered chromosome string
	 */
	private String[] crossChromosome(String m, String f) {
		StringBuilder chrStringA = new StringBuilder();
		StringBuilder chrStringB = new StringBuilder();
		String toReturn[] = new String[2];
		// if crossover procs
		if (rnd.nextInt(100) < crossRate*100) {
			/**
			 * if crossover is randomized, select a random crossover type
			 * then revert back to random later
			 */
			boolean flag = false;
			// find random crossover type
			if (crType == 4) { crType = rnd.nextInt(3); flag = true; }
			for (int i = 0; i < f.length(); i++) { // for each gene
				if (crType == 1) {
					/**
					 * if one point crossover, find one point
					 * in the chromosome where genes before that
					 * point are from parent1 and after, from parent2
					 * to avoid full reduplication of chromosome
					 * set the bound to 1..len-1 otherwise you could
					 * get duplication of parent, bypassing crossover entirely
					 */ 
					int crossPoint = rnd.nextInt(m.length() - 1) + 1;
					if (i < crossPoint) {
						chrStringA.append(f.charAt(i));
						chrStringB.append(m.charAt(i));
					} else {
						chrStringA.append(m.charAt(i));
						chrStringB.append(f.charAt(i));
					}
				} else if (crType == 2) {
					// if two point crossover, instead crossover based on two pivots
					int crossPoint = rnd.nextInt(m.length() - 1) + 1;
					// second crosspoint is crosspoint..m.length
					int secondCrossPoint = rnd.nextInt(m.length() - crossPoint + 1) + crossPoint - 1;
					if (i < crossPoint) {
						chrStringA.append(f.charAt(i));
						chrStringB.append(m.charAt(i));
					} else if (i < secondCrossPoint) {
						chrStringA.append(m.charAt(i));
						chrStringB.append(f.charAt(i));
					} else {
						chrStringA.append(f.charAt(i));
						chrStringB.append(m.charAt(i));
					}
				} else {
					/**
					 * if uniform crossover, however, combine
					 * both parents with equal probability over
					 * each gene
					 */
					if (rnd.nextInt(2) == 1) {
						chrStringA.append(f.charAt(i));
						chrStringB.append(m.charAt(i));
					} else {
						chrStringA.append(m.charAt(i));
						chrStringB.append(f.charAt(i));
					}
				}
			}
			// put it back to random
			if (flag = true) { crType = 4; flag = false; }
		} else {
			// if no crossover is happening, return both verbatim
			toReturn[0] = f; toReturn[1] = m;
			return toReturn;
		}
		// return the crossovered chromosomes
		toReturn[0] = chrStringA.toString(); toReturn[1] = chrStringB.toString();
		return toReturn;
	}
	
	/**
	 * method to mutate a chromosome with random genes
	 * will mutate each character based on proc chance
	 * mutation operator is boundary flipping
	 * @param k - the input chromosome
	 * @return - the mutated chromosome
	 */
	private String mutateChromosome(String k) {
		StringBuilder chrString = new StringBuilder();
		for (int i = 0; i < k.length(); i++) { // for each gene
			// if mutation procs
			if (rnd.nextInt(100) < mutatRate*100) {
				// assign a random gene as mutation
				chrString.append(OPTIONS.charAt(rnd.nextInt(27)));
			} else {
				// otherwise, use verbatim
				chrString.append(k.charAt(i));
			}
		}
		return chrString.toString();
	}		

	/**
	 * method to initialize initial population
	 */
	private void initializePopulation() {
		for (int i = 0; i < maxPop; i++) { // for each population size
			String chromosome = getRandChromosome(crSize);
			chrColl.add(new KeyScorePair(chromosome, encText));
		}
	}
	
	/**
	 * method to generate random chromosome
	 * @param s - the size of the chromosome
	 * @return - a randomized chromosome of valid chars
	 */
	private String getRandChromosome(int s) {
		StringBuilder chromosome = new StringBuilder(); // chromosome to return later
		for (int i = 0; i < s; i++) {
			// random gene from valid characters
			chromosome.append(OPTIONS.charAt(rnd.nextInt(27)));
		}
		return chromosome.toString();
	}
	
	/**
	 * method to output the generation number and best chromosome
	 * @param k - the best chromosome
	 * @param g - the generation number
	 */
	private void bestInGeneration(KeyScorePair k, int g) {
		double count = 0.00;
		for (int i = 0; i < chrColl.size(); i++) {
			count += chrColl.get(i).getScore();
		}
		count = floor(100*(count/chrColl.size()))/100;
		String keyScore = "(" + k.getKey() + ", " + floor(10000*k.getScore())/10000 + ")";
		System.out.println("Generation " + g + ", best is: " + keyScore + ", avg: " + count);
	}
	
	/**
	 * method to output the best chromosome in run
	 * @param k - the best chromosome
	 */
	private void bestInRun(KeyScorePair k) {
		System.out.println("\nBest chromosome was \"" + k.getKey() + "\" with fitness " + floor(10000*k.getScore())/10000+".\n");
	}
}