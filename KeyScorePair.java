import static java.lang.Math.abs;

/**
 * key-score pair class
 */
public class KeyScorePair {
	private final String key; // the chromosome
	private final double score; // the fitness
	
	// constructor
	public KeyScorePair (String pass, String crypted) {
		key = pass;
		score = fitness(pass, crypted);
	}
	
	// getter methods
	public String getKey() { return key; }
	public double getScore() { return score; }
	
	private double fitness(String k, String c) {
		double[] frequencies =	{0.0850, 0.0160, 0.0316, 0.0387, 0.1210,
								 0.0218, 0.0209, 0.0496, 0.0733, 0.0022,
								 0.0081, 0.0421, 0.0252, 0.0717, 0.0747,
								 0.0207, 0.0010, 0.0633, 0.0673, 0.0894,
								 0.0268, 0.0106, 0.0183, 0.0019, 0.0172,
								 0.0011};
		String d = c.toLowerCase();
		d = d.replaceAll("[^a-z]", "");
		d = d.replaceAll("\\s", "");
		int[] cipher = new int[d.length()];
		for (int x = 0; x < d.length(); x++) {
			cipher[x] = ((int)d.charAt(x)) - 97;
		}
		String ke = k.toLowerCase();
		ke = ke.replaceAll("[^a-z]", "");
		ke = ke.replaceAll("\\s", "");
		char[] key = ke.toCharArray();
		for (int i = 0; i < key.length; i++) { key[i] = (char)(key[i] - 97); }
		int[] charCounts = new int[26];
		for (int i = 0; i < charCounts.length; i++) { charCounts[i] = 0; }
		int[] plain = new int[cipher.length];
		int keyPtr = 0;
		for (int i = 0; i < cipher.length; i++) {
			char keyChar = (char)0;
			if (key.length > 0) {
				while (key[keyPtr] > 25 || key[keyPtr] < 0) {
					keyPtr = (keyPtr + 1) % key.length;
				}
				keyChar = key[keyPtr];
				keyPtr = (keyPtr + 1) % key.length;
			}
			plain[i] = ((26 + cipher[i] - keyChar) % 26);
		}
		for (int x : plain) {
			charCounts[x]++;
		}
		double score = 0;
		for (int y = 0; y < charCounts.length; y++) {
			score += abs((((float)charCounts[y])/plain.length)-frequencies[y]);
		}
		return score;
	}
};