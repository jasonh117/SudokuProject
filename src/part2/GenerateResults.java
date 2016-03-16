package part2;

import java.util.ArrayList;

import cspSolver.BTSolver;
import cspSolver.BTSolver.Preprocessing;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;

public class GenerateResults {

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println("Avergage nodes		Average time		Std Dev Time		Tokens");
		String[] filenames = {"ExampleSudokuFiles/PH1.txt", "ExampleSudokuFiles/PH2.txt", "ExampleSudokuFiles/PH3.txt", "ExampleSudokuFiles/PH4.txt", "ExampleSudokuFiles/PH5.txt"};
		String[] tokens = {"", "FC", "MRV", "DH", "LCV", "ACP", "MAC", "FC MRV DH LCV", "ACP MAC MRV DH LCV"};
		for (String token: tokens)
		{
			double[] times = new double[filenames.length];
			double[] nodes = new double[filenames.length];
			
			for (int i = 0; i < filenames.length; i++)
			{
				SudokuFile sf = SudokuBoardReader.readFile(filenames[i]);
				BTSolver solver = runsolver(sf, token);
				times[i] = solver.getTimeTaken()/1000.0;
				nodes[i] = solver.getNumAssignments();
			}
			
			Statistics stat1 = new Statistics(nodes);
			Statistics stat2 = new Statistics(times);
			
			System.out.format("%-10d %22.7f %23.7f \t\t%s", Math.round(stat1.getMean()), stat2.getMean(), stat2.getStdDev(), token);
		}
	}
	
	public static BTSolver runsolver(SudokuFile sf, String args)
	{
		BTSolver solver = new BTSolver(sf, 300);
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
			t1.join();
			if(t1.isAlive())
			{
				t1.interrupt();
			}
		}catch(InterruptedException e)
		{
		}
		return solver;
	}
	
	private static ArrayList<String> getTokens(String args)
	{
		ArrayList<String> tokens = new ArrayList<String>();
		String[] splited = args.split("\\s+");
		for (String token: splited)
		{
			tokens.add(token);
		}
		return tokens;
	}
}
