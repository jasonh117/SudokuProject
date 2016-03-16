package part4;

import cspSolver.BTSolver;
import cspSolver.BTSolver.Preprocessing;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuFile;

public class GenerateResultsPart4 {

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println("m    Avergage nodes 	Average time		Std Dev Time		Solvable");

		int[] p = {3,3,4,3,4,3,4,3,4,5,4,5};
		int[] q = {4,5,4,6,5,7,6,9,7,6,8,7};
		for (int i = 0; i < p.length; i++)
		{
			int n = p[i] * q[i];
			int m = (int) Math.round(Math.pow(n, 2) * .222);
			double[] times = new double[10];
			double[] nodes = new double[10];
			double[] solvable = new double[10];
			for (int j = 0; j < 10; j++)
			{
				SudokuFile sf = SudokuBoardGenerator.generateBoard(n, p[i], q[i], m);
				BTSolver solver = runsolver(sf);
				solvable[j] = solver.getStatus() != "timeout" ? 1 : 0;
				times[j] = solvable[j] == 1 ? solver.getTimeTaken()/1000.0 : -1 ;
				nodes[j] = solvable[j] == 1 ? solver.getNumAssignments() : -1;
				Thread.sleep(1000);
			}
			
			Statistics stat1 = new Statistics(nodes);
			Statistics stat2 = new Statistics(times);
			Statistics stat3 = new Statistics(solvable);
			
			System.out.format("%-4d %-10d %22.7f %23.7f %18.2f%n", m, Math.round(stat1.getMean()), stat2.getMean(), stat2.getStdDev(), stat3.getMean());
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
