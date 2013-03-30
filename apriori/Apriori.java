package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		DataSet dataSet = readData(Config.filename);
		ArrayList<NonDuplicateArrayList<Integer>> transactions = getTransactions(dataSet);

		// System.out.println("# of users: " + dataSet.getUsers().size());
		// System.out.println("# of trajectories: " + dataSet.getTotalNumOfTrajectories());
		// System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
		// System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
		// System.out.println("# of transactions: " + transactions.size());

		int[] hashTable = new int[Math.calculateCombination(dataSet.getLocations().getLocationMap().size(), 2)];
		HashMap<NonDuplicateArrayList<Integer>, Integer> candidates = new HashMap<NonDuplicateArrayList<Integer>, Integer>();
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
}
