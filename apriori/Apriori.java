package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
		int iteration;
		DataSet dataSet = readData(Config.filename);
		ArrayList<NonDuplicateArrayList<Integer>> transactions = getTransactions(dataSet);

		// System.out.println("# of users: " + dataSet.getUsers().size());
		// System.out.println("# of trajectories: " + dataSet.getTotalNumOfTrajectories());
		// System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
		// System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		// System.out.println("# of transactions: " + transactions.size());

		Config.minSupCount = (int) (transactions.size() * Config.minSup);
		int[] hashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), 2)];
		HashMap<NonDuplicateArrayList<Integer>, Integer> candidates = new HashMap<NonDuplicateArrayList<Integer>, Integer>();
		ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet = new ArrayList<NonDuplicateArrayList<Integer>>();
		for (NonDuplicateArrayList<Integer> transaction: transactions){
			for (NonDuplicateArrayList<Integer> subset: getKSubset(transaction, 1)){
				if (candidates.containsKey(subset))
					candidates.put(subset, candidates.get(subset) + 1);
				else
					candidates.put(subset, 1);
			}
			for (NonDuplicateArrayList<Integer> subset: getKSubset(transaction, 2)){
				hashTable[Math.hash(subset, hashTable.length)]++;
			}
		}
		frequentItemSet.clear();
		for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
			if (entry.getValue() > Config.minSupCount)
				frequentItemSet.add(entry.getKey());
		}
		printFrequentItemSet(frequentItemSet);

		iteration = 2;
		while (frequentItemSet.size() > 0){
			generateCandidates(frequentItemSet, hashTable, candidates);
			int[] newHashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), iteration + 1)];
			ArrayList<NonDuplicateArrayList<Integer>> newTransactions = new ArrayList<NonDuplicateArrayList<Integer>>();
			for (NonDuplicateArrayList<Integer> transaction: transactions){
				NonDuplicateArrayList<Integer> newTransaction = new NonDuplicateArrayList<Integer>();
				countSupport(transaction, candidates, iteration, newTransaction);
				if (newTransaction.size() > iteration){
					NonDuplicateArrayList<Integer> newnewTransaction = new NonDuplicateArrayList<Integer>();
					makeHashTable(newTransaction, hashTable, iteration, newHashTable, newnewTransaction);
					if (newnewTransaction.size() > iteration)
						newTransactions.add(newnewTransaction);
				}
			}
			frequentItemSet.clear();
			for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
				if (entry.getValue() > Config.minSupCount)
					frequentItemSet.add(entry.getKey());
			}
			printFrequentItemSet(frequentItemSet);
			iteration++;
		}
	}

	private void generateCandidates(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet, 
									int[] hashTable, 
									HashMap<NonDuplicateArrayList<Integer>, Integer> candidates){
		candidates.clear();

	}

	private void countSupport(NonDuplicateArrayList<Integer> transaction, 
								HashMap<NonDuplicateArrayList<Integer>, Integer> candidates, 
								int iteration, 
								NonDuplicateArrayList<Integer> newTransaction){
		int[] occurrenceCount = new int[transaction.size()];
		// for (NonDuplicateArrayList<Integer> candidate: candidates){
		for (Entry<NonDuplicateArrayList<Integer>, Integer> entry: candidates.entrySet()){
			if (transaction.contains(entry.getKey())){
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
		for (NonDuplicateArrayList<Integer> subset: getKSubset(newTransaction, iteration + 1)){

		}
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
		ArrayList<NonDuplicateArrayList<Integer>> subsets = new ArrayList<NonDuplicateArrayList<Integer>>();
		Integer[] subset = new Integer[k];
		processLargerSubsets(set, subset, 0, 0, subsets);
		return subsets;
	}

	private void processLargerSubsets(NonDuplicateArrayList<Integer> set, Integer[] subset, int subsetSize, int nextIndex, ArrayList<NonDuplicateArrayList<Integer>> subsets){
		if (subsetSize == subset.length){
			subsets.add(new NonDuplicateArrayList<Integer>(subset));
		}
		else{
			for (int j = nextIndex; j < set.size(); j++){
				subset[subsetSize] = set.get(j);
				processLargerSubsets(set, subset, subsetSize + 1, j + 1, subsets);
			}
		}
	}

	private void printFrequentItemSet(ArrayList<NonDuplicateArrayList<Integer>> frequentItemSet){
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
			System.out.println(string);
	}
}
