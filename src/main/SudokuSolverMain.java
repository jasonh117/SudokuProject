package main;
import java.util.ArrayList;

import cspSolver.BTSolver;
import cspSolver.BTSolver.Preprocessing;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardReader;
import sudoku.SudokuBoardWriter;
import sudoku.SudokuFile;

public class SudokuSolverMain {

	public static void main(String[] args)
	{
		long totalStartTime = System.currentTimeMillis();
		SudokuFile sf = SudokuBoardReader.readFile(args[0]);
		BTSolver solver = new BTSolver(sf, Integer.parseInt(args[2]));
		ArrayList<String> tokens = getTokens(args);
		
		if (tokens.contains("ACP")) solver.setACPreprocessing(Preprocessing.ACPreprocessing);
		if (tokens.contains("FC")) solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
		if (tokens.contains("MAC")) solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
		if (tokens.contains("MRV")) solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
		if (tokens.contains("DH")) solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
		if (tokens.contains("MRV") && tokens.contains("DH")) solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MRVDH);
		if (tokens.contains("LCV")) solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
		
		Thread t1 = new Thread(solver);
		try
		{
			t1.start();
			t1.join(600000);
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
		}

		SudokuBoardWriter.writeFile(solver, args[1], totalStartTime);
	}
	
	private static ArrayList<String> getTokens(String[] args)
	{
		ArrayList<String> tokens = new ArrayList<String>();
		for (int i = 3; i < args.length; i++)
		{
			tokens.add(args[i]);
		}
		return tokens;
	}
}
