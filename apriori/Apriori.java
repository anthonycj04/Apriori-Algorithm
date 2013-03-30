package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jsonobject.DataSet;
import jsonobject.User;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Implements the Apriori Algorithm(with Direct Hashing and Pruning)
 * Reference: 	An Effective Hash_Based Algorithm for Mining Association Rules
 * 				Jong Soo Park, Ming-Syan Chen, Philip S. Yu
 */

public class Apriori {
	public static void main(String[] args){
		Apriori apriori = new Apriori();
		apriori.start();
	}
	
	public Apriori(){
	}

	public void start(){
		int iteration, numOfFrequentItemSets = 0, numOfFreqItems = 0, remainingItems = 0;
		DataSet dataSet = readData(Config.filename);
		ArrayList<NonDuplicateArrayList<Integer>> transactions = getTransactions(dataSet);
		long startTime = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#.#####");
		PrintWriter printWriter = null;
		String outFilename = Config.filename + "_" + df.format(Config.minSup) + ".txt";
		try {
			printWriter = new PrintWriter(new FileOutputStream(outFilename, false), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		remainingItems = dataSet.getLocations().getLocationMap().size();
		Config.minSupCount = (int) (transactions.size() * Config.minSup);
		int[] hashTable = new int[Math.calculateCombination(remainingItems, 2)];
		HashMap<NonDuplicateArrayList<Integer>, Integer> candidates = new HashMap<NonDuplicateArrayList<Integer>, Integer>();
		ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet = new ArrayList<NonDuplicateArrayList<Integer>>();
		for (NonDuplicateArrayList<Integer> transaction: transactions){
			ArrayList<NonDuplicateArrayList<Integer>> kSubset = getKSubset(transaction, 1);
			// for (NonDuplicateArrayList<Integer> subset: getKSubset(transaction, 1)){
			for (NonDuplicateArrayList<Integer> subset: kSubset){
				if (candidates.containsKey(subset))
					candidates.put(subset, candidates.get(subset) + 1);
				else
					candidates.put(subset, 1);
			}
			kSubset = getKSubset(transaction, 2);
			// for (NonDuplicateArrayList<Integer> subset: getKSubset(transaction, 2)){
			for (NonDuplicateArrayList<Integer> subset: kSubset){
				hashTable[Math.hash(subset, hashTable.length)]++;
			}
		}
		frequentItemSet.clear();
		for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
			if (entry.getValue() > Config.minSupCount)
				frequentItemSet.add(entry.getKey());
		}
		printFrequentItemSet(frequentItemSet, printWriter);
		numOfFreqItems += frequentItemSet.size();
		numOfFrequentItemSets += frequentItemSet.size();

		iteration = 2;
		while (frequentItemSet.size() > 0){
			System.out.println("generating candidates in iteration: " + iteration);
			generateCandidates(frequentItemSet, hashTable, candidates, iteration);
			int[] newHashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), iteration + 1)];
			ArrayList<NonDuplicateArrayList<Integer>> newTransactions = new ArrayList<NonDuplicateArrayList<Integer>>();
			for (NonDuplicateArrayList<Integer> transaction: transactions){
				NonDuplicateArrayList<Integer> newTransaction = new NonDuplicateArrayList<Integer>();
				System.out.println("counting support in iteration: " + iteration);
				countSupport(transaction, candidates, iteration, newTransaction);
				if (newTransaction.size() > iteration){
					NonDuplicateArrayList<Integer> newnewTransaction = new NonDuplicateArrayList<Integer>();
					System.out.println("making hash table in iteration: " + iteration + " with transaction:(" + newTransaction.size() + ") " + newTransaction.toString());
					makeHashTable(newTransaction, hashTable, iteration, newHashTable, newnewTransaction);
					if (newnewTransaction.size() > iteration)
						newTransactions.add(newnewTransaction);
				}
			}
			hashTable = newHashTable;
			transactions = newTransactions;
			frequentItemSet.clear();
			System.out.println("generating frequent itemsets in iteration: " + iteration);
			for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
				if (entry.getValue() >= Config.minSupCount)
					frequentItemSet.add(entry.getKey());
			}
			printFrequentItemSet(frequentItemSet, printWriter);
			numOfFrequentItemSets += frequentItemSet.size();
			numOfFreqItems += frequentItemSet.size() * iteration;

			iteration++;
		}

		long endTime = System.currentTimeMillis();
		printWriter.println();
		printWriter.println("----------------------------------------------------");
		printWriter.println();
		printWriter.println("Running time: " + (double)(endTime - startTime) / 1000);
		printWriter.println("Number of frequent itemsets: " + numOfFrequentItemSets);
		printWriter.println("Average items per frequent itemsets: " + (double) (numOfFreqItems / numOfFrequentItemSets));
		// System.out.println("# of users: " + dataSet.getUsers().size());
		// System.out.println("# of trajectories: " + dataSet.getTotalNumOfTrajectories());
		// System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
		// System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		// System.out.println("# of transactions: " + transactions.size());
		printWriter.close();
		System.out.println("Done");
	}

	private void generateCandidates(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, 
									int[] hashTable, 
									HashMap<NonDuplicateArrayList<Integer>, Integer> candidates, int iteration){
		candidates.clear();
		for(int i = 0; i < frequentItemSet.size(); i++){
			for (int j = i + 1; j < frequentItemSet.size(); j++){
				NonDuplicateArrayList<Integer> tempSet = new NonDuplicateArrayList<Integer>(frequentItemSet.get(i));
				tempSet.retainAll(frequentItemSet.get(j));
				if (tempSet.size() == iteration - 2){
					tempSet = new NonDuplicateArrayList<Integer>(frequentItemSet.get(i));
					tempSet.addAll(frequentItemSet.get(j));
					if (hashTable[Math.hash(tempSet, hashTable.length)] >= Config.minSupCount){
					candidates.put(tempSet, 0);
					}
				}
			}
		}
	}

	private void countSupport(NonDuplicateArrayList<Integer> transaction, 
								HashMap<NonDuplicateArrayList<Integer>, Integer> candidates, 
								int iteration, 
								NonDuplicateArrayList<Integer> newTransaction){
		int[] occurrenceCount = new int[transaction.size()];
		// for (NonDuplicateArrayList<Integer> candidate: candidates){
		for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
			if (transaction.containsAll(entry.getKey())){
				candidates.put(entry.getKey(), entry.getValue() + 1);
				for (Integer i: entry.getKey())
					occurrenceCount[transaction.indexOf(i)]++;
			}
		}
		for (int i = 0; i < transaction.size(); i++){
			if (occurrenceCount[i] >= iteration)
				newTransaction.add(transaction.get(i));
		}
	}

	private void makeHashTable(NonDuplicateArrayList<Integer> newTransaction, 
								int[] hashTable, 
								int iteration, 
								int[] newHashTable, 
								NonDuplicateArrayList<Integer> newnewTransaction){
		int[] occurrenceCount = new int[newTransaction.size()];
		ArrayList<NonDuplicateArrayList<Integer>> kSubset = getKSubset(newTransaction, iteration + 1);
		// for (NonDuplicateArrayList<Integer> subset: getKSubset(newTransaction, iteration + 1)){
		for (NonDuplicateArrayList<Integer> subset: kSubset){
			boolean flag = true;
			ArrayList<NonDuplicateArrayList<Integer>> innerKSubset = getKSubset(subset, iteration);
			// for (NonDuplicateArrayList<Integer> subsubset: getKSubset(subset, iteration)){
			for (NonDuplicateArrayList<Integer> subsubset: innerKSubset){
				if (hashTable[Math.hash(subsubset, hashTable.length)] < Config.minSupCount){
					flag = false;
					break;
				}
			}
			if (flag){
				newHashTable[Math.hash(subset, newHashTable.length)]++;
				for (Integer i: subset)
					occurrenceCount[newTransaction.indexOf(i)]++;
			}
		}
		for (int i = 0; i < newTransaction.size(); i++){
			if (occurrenceCount[i] > 0)
				newnewTransaction.add(newTransaction.get(i));
		}
		System.out.println("out of makeHashTable");
	}

	// reads a json string from a given file and convert it into a java class
	private DataSet readData(String filename){
		if ((new File(filename).exists())){
			// file exists, start reading data
			try {
				String line;
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
				// while ((line = bufferedReader.readLine()) != null);
				line = bufferedReader.readLine();
				bufferedReader.close();
				ObjectMapper mapper = new ObjectMapper();
				DataSet dataSet = mapper.readValue(line, DataSet.class);
				return dataSet;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			// file doesn't exist
			System.err.println("file doesn't exist");
			System.exit(1);
		}
		return null;
	}

	// retrieve the transactions from a given dataset
	private ArrayList<NonDuplicateArrayList<Integer>> getTransactions(DataSet dataSet){
		ArrayList<NonDuplicateArrayList<Integer>> transactions = new ArrayList<NonDuplicateArrayList<Integer>>();
		List<User> users = dataSet.getUsers();
		// many users
		for (User user: users){
			List<List<List<String>>> trajectories = user.getTrajectories();
			// each user has one or more trajectories
			for (List<List<String>> trajectory: trajectories){
				// each trajectory contains one or more records
				NonDuplicateArrayList<Integer> transaction = new NonDuplicateArrayList<Integer>();
				for (List<String> record: trajectory)
					transaction.add(Integer.valueOf(record.get(0)));
				transactions.add(transaction);
			}
		}
		return transactions;
	}

	private ArrayList<NonDuplicateArrayList<Integer>> getKSubset(NonDuplicateArrayList<Integer> set, int k){
		/*
		// recursive approach
		// ref: http://stackoverflow.com/questions/4504974/how-to-iteratively-generate-k-elements-subsets-from-a-set-of-size-n-in-java
		ArrayList<NonDuplicateArrayList<Integer>> subsets = new ArrayList<NonDuplicateArrayList<Integer>>();
		Integer[] subset = new Integer[k];
		processLargerSubsets(set, subset, 0, 0, subsets);
		return subsets;
		*/
		// iterative approach
		// ref: http://stackoverflow.com/questions/4504974/how-to-iteratively-generate-k-elements-subsets-from-a-set-of-size-n-in-java
		// int[] setOfOnes = new int[set.size()];
		// Arrays.fill(setOfOnes, 1);
		int c = (int) Math.binomial(set.size(), k);
		ArrayList<NonDuplicateArrayList<Integer>> subsets = new ArrayList<NonDuplicateArrayList<Integer>>();
		for (int i = 0; i < c; i++)
			subsets.add(new NonDuplicateArrayList<Integer>());
		// int[][] res = new int[c][k];
		int[] ind = k < 0?null:new int[k];
		for (int i = 0; i < k; i++)
			ind[i] = i;
		for (int i = 0; i < c; i++){
			for (int j = 0; j < k; j++){
				// res[i][j] = setOfOnes[ind[j]];
				subsets.get(i).add(set.get(ind[j]));
			}
			int x = ind.length - 1;
			boolean loop;
			do{
				loop = false;
				ind[x] = ind[x] + 1;
				if (ind[x] > set.size() - (k - x)){
					x--;
					loop = x >= 0;
				}
				else{
					for (int x1 = x + 1; x1 < ind.length; x1++)
						ind[x1] = ind[x1 - 1] + 1;
				}
			} while (loop);
		}
		// for (int[] i: res){
		// 	for (int j: i)
		// 		System.out.print(j + " ");
		// 	System.out.println();
		// }
		return subsets;
	}

	/*private void processLargerSubsets(NonDuplicateArrayList<Integer> set, Integer[] subset, int subsetSize, int nextIndex, ArrayList<NonDuplicateArrayList<Integer>> subsets){
		if (subsetSize == subset.length){
			subsets.add(new NonDuplicateArrayList<Integer>(subset));
		}
		else{
			for (int j = nextIndex; j < set.size(); j++){
				subset[subsetSize] = set.get(j);
				processLargerSubsets(set, subset, subsetSize + 1, j + 1, subsets);
			}
		}
	}*/

	private void printFrequentItemSet(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, PrintWriter printWriter){
		ArrayList<String> result = new ArrayList<String>();
		for (NonDuplicateArrayList<Integer> set: frequentItemSet){
			ArrayList<String> tempArray = new ArrayList<String>();
			for (Integer i: set)
				tempArray.add(String.valueOf(i));
			Collections.sort(tempArray);
			String tempString = "";
			for (String string: tempArray)
				tempString += string + ";";
			tempString = tempString.substring(0, tempString.length() - 1);
			result.add(tempString);
		}
		Collections.sort(result);
		for (String string: result)
			printWriter.println(string);
	}
}
