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

