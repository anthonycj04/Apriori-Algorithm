package apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
		readData();
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
