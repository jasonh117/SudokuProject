package part3;

import cspSolver.BTSolver;
import cspSolver.BTSolver.Preprocessing;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuFile;

public class GenerateResultsPart3 {

	public static void main(String[] args) throws InterruptedException
	{
		int[] m = {4,8,12,16,17,18,19,20,21,22,24,28,32,36};
		for (int i = 0; i < m.length; i++)
		{
			double[] times = new double[10];
			double[] nodes = new double[10];
			double[] solvable = new double[10];
			for (int j = 0; j < 10; j++)
			{
				SudokuFile sf = SudokuBoardGenerator.generateBoard(9, 3, 3, m[i]);
				BTSolver solver = runsolver(sf);
				times[j] = solver.getTimeTaken()/1000.0;
				nodes[j] = solver.getNumAssignments();
				solvable[j] = solver.getStatus() == "success" ? 1 : 0;
				Thread.sleep(1000);
			}
			
			Statistics stat1 = new Statistics(nodes);
			Statistics stat2 = new Statistics(times);
			Statistics stat3 = new Statistics(solvable);
			
			System.out.println("Average nodes=" + stat1.getMean()+ "	Average time=" + stat2.getMean() + "	Average STD time=" + stat2.getStdDev() + "	solvable=" + stat3.getMean());
		}
	}
	
	public static BTSolver runsolver(SudokuFile sf)
	{
		BTSolver solver = new BTSolver(sf, 300);
		
		solver.setACPreprocessing(Preprocessing.ACPreprocessing);
//		solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
		solver.setConsistencyChecks(ConsistencyCheck.ArcConsistency);
//		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
//		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
		solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MRVDH);
		solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
		
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
}
