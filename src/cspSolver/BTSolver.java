package cspSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import sudoku.Converter;
import sudoku.SudokuFile;
/**
 * Backtracking solver. 
 *
 */
public class BTSolver implements Runnable{

	//===============================================================================
	// Properties
	//===============================================================================

	private ConstraintNetwork network;
	private static Trail trail = Trail.getTrail();
	private boolean hasSolution = false;
	private SudokuFile sudokuGrid;
	private String status = "error";

	private int numAssignments;
	private int numBacktracks;
	private int timelimit;
	private long startTime;
	private long endTime;
	private long acPreStartTime;
	private long acPreEndTime;
	
	public enum VariableSelectionHeuristic 	{ None, MinimumRemainingValue, Degree, MRVDH };
	public enum ValueSelectionHeuristic 		{ None, LeastConstrainingValue };
	public enum ConsistencyCheck				{ None, ForwardChecking, ArcConsistency };
	public enum Preprocessing				{ None, ACPreprocessing };
	
	private VariableSelectionHeuristic varHeuristics;
	private ValueSelectionHeuristic valHeuristics;
	private ConsistencyCheck cChecks;
	private Preprocessing Preprocess;
	//===============================================================================
	// Constructors
	//===============================================================================

	public BTSolver(SudokuFile sf, int timelimit)
	{
		this.network = Converter.SudokuFileToConstraintNetwork(sf);
		this.sudokuGrid = sf;
		this.timelimit = timelimit;
		numAssignments = 0;
		numBacktracks = 0;
		Preprocess = Preprocessing.None;
		cChecks = ConsistencyCheck.None;
		valHeuristics = ValueSelectionHeuristic.None;
		varHeuristics = VariableSelectionHeuristic.None;
	}

	//===============================================================================
	// Modifiers
	//===============================================================================
	
	public void setVariableSelectionHeuristic(VariableSelectionHeuristic vsh)
	{
		this.varHeuristics = vsh;
	}
	
	public void setValueSelectionHeuristic(ValueSelectionHeuristic vsh)
	{
		this.valHeuristics = vsh;
	}
	
	public void setConsistencyChecks(ConsistencyCheck cc)
	{
		this.cChecks = cc;
	}
	
	public void setACPreprocessing(Preprocessing cc)
	{
		this.Preprocess = cc;
	}
	//===============================================================================
	// Accessors
	//===============================================================================

	/** 
	 * @return true if a solution has been found, false otherwise. 
	 */
	public boolean hasSolution()
	{
		return hasSolution;
	}

	/**
	 * @return solution if a solution has been found, otherwise returns the unsolved puzzle.
	 */
	public SudokuFile getSolution()
	{
		return sudokuGrid;
	}

	public void printSolverStats()
	{
		System.out.println("Time taken:" + (endTime-startTime) + " ms");
		System.out.println("Number of assignments: " + numAssignments);
		System.out.println("Number of backtracks: " + numBacktracks);
	}

	/**
	 * 
	 * @return time required for the solver to attain in seconds
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
	public long getEndTime()
	{
		return endTime;
	}
	
	public long getTimeTaken()
	{
		return (acPreEndTime-acPreStartTime) + (endTime-startTime);
	}
	
	public long getACPreStartTime()
	{
		return acPreStartTime;
	}
	
	public long getACPreEndTime()
	{
		return acPreEndTime;
	}

	public int getNumAssignments()
	{
		return numAssignments;
	}

	public int getNumBacktracks()
	{
		return numBacktracks;
	}

	public ConstraintNetwork getNetwork()
	{
		return network;
	}
	
	public String getStatus()
	{
		return status;
	}

	//===============================================================================
	// Helper Methods
	//===============================================================================

	/**
	 * Checks whether the changes from the last time this method was called are consistent. 
	 * @return true if consistent, false otherwise
	 */
	private boolean checkConsistency()
	{
		boolean isConsistent = false;
		switch(cChecks)
		{
		case None: 				isConsistent = assignmentsCheck();
		break;
		case ForwardChecking: 	isConsistent = forwardChecking();
		break;
		case ArcConsistency: 	isConsistent = arcConsistency();
		break;
		default: 				isConsistent = assignmentsCheck();
		break;
		}
		return isConsistent;
	}
	
	/**
	 * default consistency check. Ensures no two variables are assigned to the same value.
	 * @return true if consistent, false otherwise. 
	 */
	private boolean assignmentsCheck()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (v.getAssignment() == vOther.getAssignment())
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean forwardChecking()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if (!vOther.isAssigned())
						vOther.removeValueFromDomain(v.getAssignment());
					if (v.getAssignment() == vOther.getAssignment())
						return false;
				}
			}
		}
		return true;
	}

	private boolean arcConsistency()
	{
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				Queue<Variable> acq = new LinkedList<Variable>();
				acq.add(v);
				
				while (!acq.isEmpty())
				{
					Variable n = acq.remove();
					for(Variable vOther : network.getNeighborsOfVariable(n))
					{
						if (!vOther.isAssigned())
						{
							vOther.removeValueFromDomain(n.getAssignment());
							if (vOther.isAssigned() && !acq.contains(vOther))
								acq.add(vOther);
						}
						if (n.getAssignment() == vOther.getAssignment())
							return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Selects the next variable to check.
	 * @return next variable to check. null if there are no more variables to check. 
	 */
	private Variable selectNextVariable()
	{
		Variable next = null;
		switch(varHeuristics)
		{
		case None: 					next = getfirstUnassignedVariable();
		break;
		case MinimumRemainingValue: next = getMRV();
		break;
		case Degree:				next = getDegree(network.getVariables());
		break;
		case MRVDH:					next = getMRVDH();
		break;
		default:					next = getfirstUnassignedVariable();
		break;
		}
		return next;
	}
	
	/**
	 * default next variable selection heuristic. Selects the first unassigned variable. 
	 * @return first unassigned variable. null if no variables are unassigned. 
	 */
	private Variable getfirstUnassignedVariable()
	{
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				return v;
			}
		}
		return null;
	}

	private Variable getMRV()
	{
		Variable leastRemaining = null;
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				if (leastRemaining == null || v.size() < leastRemaining.size())
				{
					leastRemaining = v;
				}
			}
		}
		return leastRemaining;
	}
	
	private Variable getDegree(List<Variable> values)
	{
		Variable degreeheuristic = null;
		int max_neighbors_unassigned = 0;
		for(Variable v : values)
		{
			if(!v.isAssigned())
			{
				if (degreeheuristic == null)
					degreeheuristic = v;
				
				int neighbors_unassigned = 0;
				for(Variable vOther : network.getNeighborsOfVariable(v))
				{
					if(!vOther.isAssigned())
					{
						neighbors_unassigned++;
					}
				}
				if (neighbors_unassigned > max_neighbors_unassigned)
				{
					max_neighbors_unassigned = neighbors_unassigned;
					degreeheuristic = v;
				}
			}
		}
		return degreeheuristic;
	}
	
	private Variable getMRVDH()
	{
		List<Variable> leastRemaining = new ArrayList<Variable>();
		int minimum = -1;
		for(Variable v : network.getVariables())
		{
			if(!v.isAssigned())
			{
				if (minimum == -1 || v.size() < minimum)
				{
					minimum = v.size();
					leastRemaining.clear();
					leastRemaining.add(v);
				}
				else if (v.size() == minimum)
				{
					leastRemaining.add(v);
				}
			}
		}
		
		if (leastRemaining.isEmpty())
			return null;
		
		if (leastRemaining.size() == 1)
			return leastRemaining.get(0);
		
		return getDegree(leastRemaining);
	}
	
	/**
	 * Value Selection Heuristics. Orders the values in the domain of the variable 
	 * passed as a parameter and returns them as a list.
	 * @return List of values in the domain of a variable in a specified order. 
	 */
	public List<Integer> getNextValues(Variable v)
	{
		List<Integer> orderedValues;
		switch(valHeuristics)
		{
		case None: 						orderedValues = getValuesInOrder(v);
		break;
		case LeastConstrainingValue: 	orderedValues = getValuesLCVOrder(v);
		break;
		default:						orderedValues = getValuesInOrder(v);
		break;
		}
		return orderedValues;
	}
	
	/**
	 * Default value ordering. 
	 * @param v Variable whose values need to be ordered
	 * @return values ordered by lowest to highest. 
	 */
	public List<Integer> getValuesInOrder(Variable v)
	{
		List<Integer> values = v.getDomain().getValues();
		
		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}
	
	public List<Integer> getValuesLCVOrder(final Variable v)
	{
		List<Integer> values = v.getDomain().getValues();
		
		Comparator<Integer> valueComparator = new Comparator<Integer>(){
			
			@Override
			public int compare(Integer x, Integer y) {
				Integer xcount = 0, ycount = 0;
				
				for(Variable vOther : network.getNeighborsOfVariable(v)) {
					if(!vOther.isAssigned()) {
						for(int each : vOther.Values())
						{
							if(each == x)
								xcount++;
							if(each == y)
								ycount++;
						}
					}
				}
				
				return xcount.compareTo(ycount);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}
	/**
	 * Called when solver finds a solution
	 */
	private void success()
	{
		hasSolution = true;
		sudokuGrid = Converter.ConstraintNetworkToSudokuFile(network, sudokuGrid.getN(), sudokuGrid.getP(), sudokuGrid.getQ());
		status = "success";
	}
	
	private boolean exceedTimeLimit()
	{
		return timelimit <= ((acPreEndTime-acPreStartTime) + (System.currentTimeMillis()-startTime))/1000;
	}

	//===============================================================================
	// Solver
	//===============================================================================

	/**
	 * Method to start the solver
	 */
	public void solve()
	{
		acPreStartTime = System.currentTimeMillis();
		if (Preprocess == Preprocessing.ACPreprocessing)
			arcConsistency();
		acPreEndTime = System.currentTimeMillis();
		
		startTime = System.currentTimeMillis();
		try {
			solve(0);
		}catch (VariableSelectionException e)
		{
			System.out.println("error with variable selection heuristic.");
		}
		endTime = System.currentTimeMillis();
		Trail.clearTrail();
	}

	/**
	 * Solver
	 * @param level How deep the solver is in its recursion. 
	 * @throws VariableSelectionException 
	 */

	private void solve(int level) throws VariableSelectionException
	{
		if(!Thread.currentThread().isInterrupted())

		{//Check time limit
			if (exceedTimeLimit())
			{
				status = "timeout";
				return;
			}
			
			//Check if assignment is completed
			if(hasSolution)
			{
				return;
			}

			//Select unassigned variable
			Variable v = selectNextVariable();		

			//check if the assignment is complete
			if(v == null)
			{
				for(Variable var : network.getVariables())
				{
					if(!var.isAssigned())
					{
						throw new VariableSelectionException("Something happened with the variable selection heuristic");
					}
				}
				success();
				return;
			}

			//loop through the values of the variable being checked LCV

			
			for(Integer i : getNextValues(v))
			{
				trail.placeBreadCrumb();

				//check a value
				v.updateDomain(new Domain(i));
				numAssignments++;
				boolean isConsistent = checkConsistency();
				
				//move to the next assignment
				if(isConsistent)
				{		
					solve(level + 1);
				}

				//if this assignment failed at any stage, backtrack
				if(!hasSolution)
				{
					trail.undo();
					numBacktracks++;
				}
				
				else
				{
					return;
				}
			}	
		}
	}

	@Override
	public void run() {
		solve();
	}
}
