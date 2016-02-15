package main;
import java.util.ArrayList;

import cspSolver.BTSolver;
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
		
		solver.setConsistencyChecks(ConsistencyCheck.None);
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
		if (tokens.contains("FC")) solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
		
		
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

//		solver.printSolverStats();
//		System.out.println(solver.getSolution());	
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
