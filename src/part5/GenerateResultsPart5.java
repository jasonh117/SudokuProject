package part5;

import cspSolver.BTSolver;
import cspSolver.BTSolver.Preprocessing;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuFile;

public class GenerateResultsPart5 {

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println("m    n    p    q    r			Average nodes		Average time		Std Dev Time		Solvable");
		int p = 3;
		int q = 4;
		int n = p * q;
		double[] r = {0.0494, 0.0988, 0.148, 0.198, 0.210, 0.222, 0.235, 0.247, 0.259, 0.272, 0.296, 0.346, 0.395, 0.444};
		int[] m = new int[r.length];
		for (int i = 0; i < r.length; i++)
		{
			m[i] = (int) Math.round(Math.pow(n, 2) * r[i]);
		}

		for (int i = 0; i < m.length; i++)
		{
			int repeats = 10;
			double[] times = new double[repeats];
			double[] nodes = new double[repeats];
			double[] solvable = new double[repeats];
			for (int j = 0; j < repeats; j++)
			{
				SudokuFile sf = SudokuBoardGenerator.generateBoard(n, p, q, m[i]);
				BTSolver solver = runsolver(sf);
				times[j] = solver.getTimeTaken()/1000.0;
				nodes[j] = solver.getNumAssignments();
				solvable[j] = solver.getStatus() == "success" ? 1 : 0;
				Thread.sleep(1000);
			}
			
			Statistics stat1 = new Statistics(nodes);
			Statistics stat2 = new Statistics(times);
			Statistics stat3 = new Statistics(solvable);
			
			System.out.format("%-4d %-4d %-4d %-4d %-19f %-10d %22.7f %23.7f %18.2f%n", m[i], n, p, q, r[i], Math.round(stat1.getMean()), stat2.getMean(), stat2.getStdDev(), stat3.getMean());
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
