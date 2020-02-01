package model.initialization;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import controller.ChromosomeFactory;
import model.chromosome.Chromosome;
import model.chromosome.FSM;
import model.chromosome.FSMTest;

public class IncrementalInitialization extends Initialization {
	
	public IncrementalInitialization(int size) {
		this.initialization = 1;
		this.size = size;
	}
	
	public IncrementalInitialization() {
		this.initialization = 1;
	}
	
	public Chromosome[] initialize(int size_pob, int heur, List<FSMTest> allTests, List<FSM> mutants) {
		Chromosome[] pop = new Chromosome[size_pob];
		int[][] testsVSmutants = new int[allTests.size()][mutants.size()];
		int maxPenalty = 0;
		int minValue = 0;
		for(int test = 0; test < allTests.size(); test++)
			for(int mutant = 0; mutant < mutants.size(); mutant++) {
				if (heur == 0) {
					testsVSmutants[test][mutant] = allTests.get(test).killMutantAdd(mutants.get(mutant));
					if (testsVSmutants[test][mutant] < minValue)
						minValue = testsVSmutants[test][mutant];
				}
				else 
					testsVSmutants[test][mutant] = allTests.get(test).killMutantMul(mutants.get(mutant));
				if (testsVSmutants[test][mutant] > maxPenalty && testsVSmutants[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutants[test][mutant];
			}
		if (heur == 0)
			for(int test = 0; test < allTests.size(); test++)
				for(int mutant = 0; mutant < mutants.size(); mutant++)
					if (testsVSmutants[test][mutant] < Integer.MAX_VALUE)
						testsVSmutants[test][mutant] -= minValue;
			
		for(int i = 0; i < size_pob; i++) {
			List<FSMTest> tests = new ArrayList<FSMTest>();
			tests.add(allTests.get(rnd.nextInt(allTests.size())));
			int actualSize = tests.get(0).getSize();
			for(int j = 1; actualSize < size && j < allTests.size() && rnd.nextDouble() > 1.0/size; j++) {
				FSMTest test = allTests.get(rnd.nextInt(allTests.size()));
				if(actualSize + test.getSize() < size) {
					tests.add(test);
					actualSize += test.getSize();
				}
			}
			pop[i] = ChromosomeFactory.createChromosome(tests, size, maxPenalty);
			pop[i].evaluate(testsVSmutants);
		}
		
		try {
			PrintWriter writer = new PrintWriter("files/Matrix.txt", "UTF-8");
			for(int test = 0; test < testsVSmutants.length; test++) {
				for(int mutant = 0; mutant < testsVSmutants[0].length; mutant++) {
					writer.print(testsVSmutants[test][mutant] + " ");
				}
				writer.println();
			}
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		return pop;
		
	}

	@Override
	public String toString() {
		return "Incremental initialization";
	}
}



