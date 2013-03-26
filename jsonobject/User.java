package jsonobject;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class User{
	private String uid;
	List<List<List<String>>> trajectories;

	public String getUid(){
		return uid;
	}

	public void setUid(String uid){
		this.uid = uid;
	}

	public List<List<List<String>>> getTrajectories(){
		return trajectories;
	}

	public void setTrajectories(List<List<List<String>>> trajectories){
		this.trajectories = trajectories;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value){
		System.out.println("handleUnknown in " + User.class.toString());
	}

	@Override
	public String toString(){
		String string = "{";
		string += "trajectories: [";
		for (int i = 0; i < trajectories.size(); i++){
			string += "[";
			for (int j = 0; j < trajectories.get(i).size(); j++){
				string += "[" + trajectories.get(i).get(j).get(0) + ", " + trajectories.get(i).get(j).get(1) + "],";
			}
			string += "],";
		}
		string += "]," + "uid: " + uid + "}";

		return string;
	}
}
