# Genetic Decryption

Java implementation of a simple genetic algorithm used to decrypt keys based on English language frequency of characters. This was used to investigate the effect of each genetic parameter on overall fitness.

# Compilation and Execution

Compile with:

`$ javac runGeneticDecipher.java`

Execute with:

`$ java runGeneticDecipher <args>`

Where <args> are as follows:

| Argument | Meaning               | Type    | Range        |
| -------- | --------------------- | ------- | ------------ |
| 1        | Population Size       | Integer | [1, n]       |
| 2        | Number of Genetation  | Integer | [1, n]       |
| 3        | Chromosome Size       | Integer | [1, n]       |
| 4        | Elite Population Size | Integer | [2, m]       |
| 5        | Tournament Size       | Integer | [2, 5]       |
| 6        | Crossover Type        | Integer | [1, 4]       |
| 7        | Mutation Type         | Integer | [1, 2]       |
| 8        | Crossover Rate        | Double  | [0.00, 1.00] |
| 9        | Mutation Rate         | Double  | [0.00, 1.00] |
| 10       | Random Seed           | Integer | [1, n]       |
| 11       | Encrypted Text        | String  | N/A          |

Where `n` is any maximum and `m` is equal to half of the population size. The final argument is to a local text file.

For crossover and mutation types, the options are below:

| Value | Crossover | Mutation     |
| ------|-----------|------------- |
| 1     | 1-Point   | Uniform      |
| 2     | 2-Point   | Non-uniform  |
| 3     | Uniform   | Non-uniform* |
| 4     | Random    | N/A          |

Where `non-uniform*` means the mutation rate increases at a later time than typical non-uniform mutation (here defined as through 33% of the total generations). For `random` crossover, it randomizes which crossover operator to use for each generation.

# Dependencies

- Java installed
- GNU/Linux

# Heuristic Evaluation

The fitness is decided by how well the decrypted text follows the frequency distribution of English letters. More closely resembling English text means a better fitness.
