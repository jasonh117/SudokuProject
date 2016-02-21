package sudoku;
import cspSolver.BTSolver;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;


public class SudokuBoardWriter {

	public static void writeFile(BTSolver solver, String filePath, long totalStartTime)
	{
		try {
			File file = new File(filePath);
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("TOTAL_START=0\n");  // all other time is relative to the total start time
			bw.write("PREPROCESSING_START=" + (solver.getACPreStartTime() - totalStartTime)/1000.0 + "\n");
			bw.write("PREPROCESSING_DONE=" + (solver.getACPreEndTime() - totalStartTime)/1000.0 + "\n");
			bw.write("SEARCH_START=" + (solver.getStartTime() - totalStartTime)/1000.0 + "\n");
			bw.write("SEARCH_DONE=" + (solver.getEndTime() - totalStartTime)/1000.0 + "\n");
			bw.write("SOLUTION_TIME=" + solver.getTimeTaken()/1000.0 + "\n");
			bw.write("STATUS=" + solver.getStatus() + "\n");
			bw.write("SOLUTION=" + solution(solver.getSolution()) + "\n");
			bw.write("COUNT_NODES=" + solver.getNumAssignments() + "\n");
			bw.write("COUNT_DEADENDS=" + solver.getNumBacktracks() + "\n");
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String solution(SudokuFile sudoku) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(");
		for(int i = 0; i < sudoku.getN(); i ++)
		{
			for(int j = 0; j < sudoku.getN(); j++)
			{
				if (i != 0 || j != 0) {
					sb.append(",");
				}
				sb.append(sudoku.getBoard()[j][i]);
			}
		}
		sb.append(")");
		
		return sb.toString();
	}
}