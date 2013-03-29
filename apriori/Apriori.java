package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import jsonobject.DataSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Apriori {
	public static void main(String[] args){
		Apriori apriori = new Apriori();
		apriori.start();
	}
	
	public Apriori(){

	}

	public void start(){
		NonDuplicateArrayList<Integer> list1 = new NonDuplicateArrayList<Integer>();
		NonDuplicateArrayList<Integer> list2 = new NonDuplicateArrayList<Integer>();

		list1.add(1);
		list1.add(3);
		list1.add(5);
		list1.add(7);
		list1.add(9);

		list2.add(2);
		list2.add(7);
		list2.add(6);
		list2.add(3);
		list2.add(8);
		list2.add(2);
		list2.remove((Integer)8);

		System.out.println(list1.toString());
		System.out.println(list2.toString());
		list1.printHashSet();
		list2.printHashSet();

		list1.remove((Integer)1);
		list1.remove((Integer)5);
		list1.remove((Integer)9);

		list2.remove((Integer)2);
		list2.remove((Integer)6);

		System.out.println(list1.toString());
		System.out.println(list2.toString());
		list1.printHashSet();
		list2.printHashSet();

		System.out.println(list1.equals(list2));

		System.exit(0);
		readData();
	}

	private void addThings(Set<Integer> in){
		in.add(12);
	}

	private long calculateCombination(long n, long r){
		if (n < r)
			return 0;
		long result = n;
		for (int i = 1; i < r; i++)
			result *= (n - i);
		for (int i = 1; i <= r; i++)
			result /= i;
		return result;
	}

	private void readData(){
		String filename = "dm2013_dataset_1.dat";
		// String filename = "dm2013_dataset_sample.dat";
		// String filename = "dm2013_dataset_2.dat";
		String line;
		BufferedReader bufferedReader;
		if ((new File(filename).exists())){
			try {
				bufferedReader = new BufferedReader(new FileReader(filename));
				// while ((line = bufferedReader.readLine()) != null);
				line = bufferedReader.readLine();
				bufferedReader.close();

				ObjectMapper mapper = new ObjectMapper();
				DataSet dataSet = mapper.readValue(line, DataSet.class);
				// System.out.println(dataSet.toString());
				System.out.println("# of users: " + dataSet.getUsers().size());
				System.out.println("# of trajectories: " + dataSet.getTotalNumOfTrajectories());
				System.out.println("# of records: " + dataSet.getTotalNumOfRecords());
				System.out.println("# of distinct locations: " + dataSet.getLocations().getLocationMap().size());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		else{
			System.out.println("not exists");
		}
	}
}
