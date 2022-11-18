package sat.EfficientImplementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Assignment {
	Map<Integer, Boolean> assignment;
	Set<Integer> passivelyAssignedVariables;
	public Assignment(Assignment other){
		this.assignment = new HashMap<>(other.assignment);
		this.passivelyAssignedVariables = new HashSet<>(other.passivelyAssignedVariables);
	}
	public Assignment(Map<Integer, Boolean> map){
		this.assignment = map.entrySet().stream().collect(Collectors.toMap(e-> e.getKey(), e -> e.getValue()));
		this.passivelyAssignedVariables = new HashSet<>();
	}
	public Assignment() {
		assignment = new HashMap<>();
		passivelyAssignedVariables = new HashSet<>();
	}

	@Override
	public String toString() {
		StringBuilder mapAsString = new StringBuilder("");
		for (Integer key : assignment.keySet()) {
			mapAsString.append(key + "=" + (assignment.get(key)?"1":"0") + ",");
		}
		return mapAsString.toString();
	}

	public void set(int variable, boolean truthValue) {
		assignment.put(variable, truthValue);// assignment[variable] =
												// truthValue;
	}
	public void addPassivelyAssignedVariable(int variable){
		passivelyAssignedVariables.add(variable);
	}

	public Set<Integer> getPassivelyAssignedVariables(){
		return passivelyAssignedVariables;
	}
	public boolean get(int variable) {
		Boolean truthValue = assignment.get(variable);
		// NOTE unassigned variables are defaulted to false.
		if (truthValue == null)
			truthValue = false;
		return truthValue;// assignment[variable];
	}

	public int getNumberOfActivelyAssignedVariables() {
		return assignment.size()-passivelyAssignedVariables.size();
	}
	
	public Set<Integer> variableSet() {
		return assignment.keySet();
	}

	public Map<Integer, Boolean> getAssignment() {
		return assignment;
	}

	public void putAll(Assignment other){
		assignment.putAll(other.assignment);
	}
	public void putAll(Map<Integer, Boolean> other){
		assignment.putAll(other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Assignment that = (Assignment) o;
		return assignment.equals(that.assignment);
	}
}
