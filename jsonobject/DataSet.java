package jsonobject;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class DataSet {
	private List<User> users;
	private Location locations;

	public List<User> getUsers(){
		return users;
	}

	public void setUsers(List<User> users){
		this.users = users;
	}

	public int getTotalNumOfTrajectories(){
		int totalNumOfTrajectories = 0;
		for (int i = 0; i < users.size(); i++)
			totalNumOfTrajectories += users.get(i).getNumOfTrajectories();
		return totalNumOfTrajectories;
	}

	public int getTotalNumOfRecords(){
		int totalNumOfRecords = 0;
		for (int i = 0; i < users.size(); i++)
			totalNumOfRecords += users.get(i).getNumOfRecords();
		return totalNumOfRecords;
	}

	public Location getLocations(){
		return locations;
	}

	public void setLocations(Location locations){
		this.locations = locations;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value){
		System.out.println("handleUnknown in " + DataSet.class.toString());
	}

	@Override
	public String toString(){
		return "{users: " + users +", locations: " + locations + "}";
	}
}
