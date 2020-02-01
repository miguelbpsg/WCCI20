package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingUtilities;

import model.chromosome.Chromosome;
import model.chromosome.FSM;
import model.chromosome.FSMTest;
import model.crossover.Crossover;
import model.initialization.Initialization;
import model.mutation.Mutation;
import model.replacement.Replacement;
import model.selection.Selection;
import view.Window;

public class Controller {
	private double[] mejores;
	private double[] peores;
	private double[] medias;
	private Chromosome[] mejorHastaAhora;
	private double[] generaciones;
	private Window v;
	private Hebra hebra;
	
	public void run(int size_pop, int iters, Initialization ini, int max_tests, int heur,
					Selection s, int participantes, double victoria, double trunc,
					Crossover c, double prob_cruce,
					Mutation m, double prob_mut,
					String testsFile, String mutantsFile,
					Replacement r, double elit,
					Window v) {
		long startTime = System.currentTimeMillis();
		
		List<FSMTest> totalTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);
		
		Initialization metInic= InitializationFactory.createInitialization(ini, max_tests);
		Selection metSelec = SelectionFactory.crearSeleccion(s, participantes, victoria, trunc);
		Crossover metCruce = CrossoverFactory.createCrossover(c, prob_cruce);
		Mutation metMut = MutationFactory.createMutation(m, prob_mut, totalTests);
		
		Chromosome[] poblacion = metInic.initialize(size_pop, heur, totalTests, mutants);
		this.v = v;

		int[][] testsVSmutants = new int[totalTests.size()][mutants.size()];
		int maxPenalty = 0;
		int minValue = 0;
		for(int test = 0; test < totalTests.size(); test++)
			for(int mutant = 0; mutant < mutants.size(); mutant++) {
				if (heur == 0) {
					testsVSmutants[test][mutant] = totalTests.get(test).killMutantAdd(mutants.get(mutant));
					if (testsVSmutants[test][mutant] < minValue)
						minValue = testsVSmutants[test][mutant];
				}
				else 
					testsVSmutants[test][mutant] = totalTests.get(test).killMutantMul(mutants.get(mutant));
				if (testsVSmutants[test][mutant] > maxPenalty && testsVSmutants[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutants[test][mutant];
			}
		if (heur == 0)
			for(int test = 0; test < totalTests.size(); test++)
				for(int mutant = 0; mutant < mutants.size(); mutant++)
					if(testsVSmutants[test][mutant] < Integer.MAX_VALUE)
					testsVSmutants[test][mutant] -= minValue;

		Replacement metReempl = ReplacementFactory.createReplacement(r, elit, heur, testsVSmutants);

		
		mejores = new double[iters+1];
		peores = new double[iters+1];
		medias = new double[iters+1];
		mejorHastaAhora = new Chromosome[iters+1];
		generaciones = new double[iters+1];
		
		Chromosome mejorGlobal = score(poblacion);
		Chromosome mejor = mejorGlobal;
		mejores[0] = mejor.getFitness();
		mejorHastaAhora[0] = mejorGlobal;
		medias[0] = media(poblacion);
		peores[0] = peor(poblacion);
		generaciones[0] = 0;
        for(int i = 0; i < iters; i++) {
        	poblacion =
			metReempl.replace(
				poblacion,
				metMut.mutate(
					metCruce.cruza(
						metSelec.select(poblacion, size_pop)
					)
				)
			);
			mejor = score(poblacion);
            mejorGlobal = mejorGlobal.better(mejor);
            mejores[i+1] = mejor.getFitness();
            mejorHastaAhora[i+1] = mejorGlobal;
            medias[i+1] = media(poblacion);
            peores[i+1] = peor(poblacion);
            generaciones[i+1] = i+1;
    	
            hebra = new Hebra(mejorGlobal, i);
            hebra.start();

		}
		System.out.println(System.currentTimeMillis() - startTime);
	}

	public void generateData(String file, int profMax, int numTests) {
		generateSpecs();
		generateTests(file, profMax, numTests, "");
		generateMutants(file, "");				
	}

	private void generateSpecs() {
		List<FSM> specifications = FSM.generateSepcifications(50, 10, 50, 3, 10, 3, 10);	//TODO freely choose
		try {
			for(int i = 0; i < specifications.size(); i++) {
				PrintWriter writer = new PrintWriter("files/Spec"+i+".txt", "UTF-8");
				writer.print(specifications.get(i).toFile());
				writer.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	
	private void generateTests(String file, int profMax, int numTests, String number) {
		FSM spec = ChromosomeFactory.readSpecification(file);

		List<FSMTest> tests = spec.generateTests(profMax, numTests);
		String testsText = "";
		for(int i = 0; i < tests.size(); i++) {
			testsText += tests.get(i).toFile();
			if(i == tests.size() - 1 || tests.get(i).getSize() == 0)
				testsText += "";
			else
				testsText += "\n";
		}
		String[] text = testsText.split("\n");

		try {
			PrintWriter writer = new PrintWriter("files/Tests"+number+".txt", "UTF-8");
			for(int i = 0; i < text.length - 1; i++)
				writer.println(text[i]);
			writer.print(text[text.length-1]);
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	private void generateMutants(String file, String number) {
		FSM spec = ChromosomeFactory.readSpecification(file);
		List<FSM> mutants = spec.generateMutants(0.1);
		String mutantsText = mutants.size() + "\n";
		Set<String> inputs = spec.getInputs();
		Iterator<String> it = inputs.iterator();
		while (it.hasNext()) {
			mutantsText += it.next() + (it.hasNext() ? " " : "\n");
		}
		Set<String> outputs = spec.getOutputs();
		it = outputs.iterator();
		while (it.hasNext()) {
			mutantsText += it.next() + (it.hasNext() ? " " : "\n");
		}		
		for(int i = 0; i < mutants.size(); i++)
			mutantsText += mutants.get(i).toFile() + (i == mutants.size() - 1 ? "" : "\n");
		
		String[] text = mutantsText.split("\n");
		try {
			PrintWriter writer = new PrintWriter("files/Mutants"+number+".txt", "UTF-8");
			for(int i = 0; i < text.length - 1; i++)
				writer.println(text[i]);
			writer.print(text[text.length-1]);
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void combinatoryMul(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();
		
		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);

		int[][] testsVSmutantsMul = new int[allTests.size()][mutants.size() + 1];
		int maxPenalty = 0;
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				testsVSmutantsMul[test][mutant] = allTests.get(test).killMutantMul(mutants.get(mutant));
				if (testsVSmutantsMul[test][mutant] > maxPenalty && testsVSmutantsMul[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutantsMul[test][mutant];
			}
			testsVSmutantsMul[test][0] = test;
		}
		
		List<List<FSMTest>> subsets = new ArrayList<List<FSMTest>>();
		
		Chromosome c = bestSubset(new ArrayList<FSMTest>(), 0, subsets, allTests, max_inputs, testsVSmutantsMul, maxPenalty, 0);
		v.updateResult(c);
		System.out.println(System.currentTimeMillis() - startTime);
	}
	

	public void combinatoryAdd(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();
		
		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);

		int[][] testsVSmutantsAdd = new int[allTests.size()][mutants.size() + 1];
		int maxPenalty = 0;
		int minValue = 0;
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				testsVSmutantsAdd[test][mutant] = allTests.get(test).killMutantAdd(mutants.get(mutant));
				if (testsVSmutantsAdd[test][mutant] > maxPenalty && testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutantsAdd[test][mutant];
				if (testsVSmutantsAdd[test][mutant] < minValue)
					minValue = testsVSmutantsAdd[test][mutant];
			}
			testsVSmutantsAdd[test][0] = test;
		}

		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				if (testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					testsVSmutantsAdd[test][mutant] -= minValue;
			}
		}
		List<List<FSMTest>> subsets = new ArrayList<List<FSMTest>>();
		
		Chromosome c = bestSubset(new ArrayList<FSMTest>(), 0, subsets, allTests, max_inputs, testsVSmutantsAdd, maxPenalty, 0);
		v.updateResult(c);
		
		System.out.println(System.currentTimeMillis() - startTime);
	}

	
	public void greedyMul(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();

		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);
		
		
		int[][] testsVSmutantsMul = new int[allTests.size()][mutants.size() + 1];
		int maxPenalty = 0;
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				testsVSmutantsMul[test][mutant] = allTests.get(test).killMutantMul(mutants.get(mutant));
				if (testsVSmutantsMul[test][mutant] > maxPenalty && testsVSmutantsMul[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutantsMul[test][mutant];
			}
			testsVSmutantsMul[test][0] = test;
		}
		List<FSMTest> tests = new ArrayList<FSMTest>();
		int[][] dynamicTable = testsVSmutantsMul;
		for(int size = 0; size < max_inputs && dynamicTable.length > 0 && dynamicTable[0].length > 0;) {
			quicksort(dynamicTable, 0, dynamicTable.length -1);
			List<Integer> removedColumns = new ArrayList<Integer>();
			for(int j = 0; j < dynamicTable[0].length; j++) {
				if(dynamicTable[0][j] < Integer.MAX_VALUE)
					removedColumns.add(j);
			}
			int[][] aux = dynamicTable;
			dynamicTable = new int[aux.length - 1][aux[0].length - removedColumns.size()];
			for(int j = 0; j < aux.length - 1; j++) {
				dynamicTable[j][0] = aux[j+1][0];
				for(int k = 1, l = 1; k < aux[0].length - removedColumns.size(); l++) {
					if(!removedColumns.contains(l)) {
						dynamicTable[j][k] = aux[j+1][l];
						k++;
					}
				}
			}
			size += allTests.get(aux[0][0]).getMaxLength();
			if(size < max_inputs)
				tests.add(allTests.get(aux[0][0]));
		}
		Chromosome c = ChromosomeFactory.createChromosome(tests, max_inputs, maxPenalty);
		c.evaluate(testsVSmutantsMul);
		v.updateResult(c);
		System.out.println(System.currentTimeMillis() - startTime);
	}


	public void greedyAdd(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();

		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);
		
		
		int[][] testsVSmutantsAdd = new int[allTests.size()][mutants.size() + 1];
		int maxPenalty = 0;
		int minValue = 0;
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				testsVSmutantsAdd[test][mutant] = allTests.get(test).killMutantAdd(mutants.get(mutant));
				if (testsVSmutantsAdd[test][mutant] > maxPenalty && testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutantsAdd[test][mutant];
				if (testsVSmutantsAdd[test][mutant] < minValue)
					minValue = testsVSmutantsAdd[test][mutant];
			}
			testsVSmutantsAdd[test][0] = test;
		}
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				if (testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					testsVSmutantsAdd[test][mutant] -= minValue;
			}
		}
			
		List<FSMTest> tests = new ArrayList<FSMTest>();
		int[][] dynamicTable = testsVSmutantsAdd;
		for(int size = 0; size < max_inputs && dynamicTable.length > 0 && dynamicTable[0].length > 0;) {
			quicksort(dynamicTable, 0, dynamicTable.length -1);
			List<Integer> removedColumns = new ArrayList<Integer>();
			for(int j = 0; j < dynamicTable[0].length; j++) {
				if(dynamicTable[0][j] < Integer.MAX_VALUE)
					removedColumns.add(j);
			}
			int[][] aux = dynamicTable;
			dynamicTable = new int[aux.length - 1][aux[0].length - removedColumns.size()];
			for(int j = 0; j < aux.length - 1; j++) {
				dynamicTable[j][0] = aux[j+1][0];
				for(int k = 1, l = 1; k < aux[0].length - removedColumns.size(); l++) {
					if(!removedColumns.contains(l)) {
						dynamicTable[j][k] = aux[j+1][l];
						k++;
					}
				}
			}
			size += allTests.get(aux[0][0]).getMaxLength();
			if(size < max_inputs)
				tests.add(allTests.get(aux[0][0]));
		}
		Chromosome c = ChromosomeFactory.createChromosome(tests, max_inputs, maxPenalty);
		c.evaluate(testsVSmutantsAdd);
		v.updateResult(c);
		System.out.println(System.currentTimeMillis() - startTime);
	}

	public void greedyWeights(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();

		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);
		
		
		int[][] testsVSmutantsAdd = new int[allTests.size()][mutants.size() + 1];
		int maxPenalty = 0;
		int minValue = 0;
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				testsVSmutantsAdd[test][mutant] = allTests.get(test).killMutantAdd(mutants.get(mutant));
				if (testsVSmutantsAdd[test][mutant] > maxPenalty && testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					maxPenalty = testsVSmutantsAdd[test][mutant];
				if (testsVSmutantsAdd[test][mutant] < minValue)
					minValue = testsVSmutantsAdd[test][mutant];
			}
			testsVSmutantsAdd[test][0] = test;
		}
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 1; mutant < mutants.size(); mutant++) {
				if (testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					testsVSmutantsAdd[test][mutant] -= minValue;
			}
		}
			
		List<FSMTest> tests = new ArrayList<FSMTest>();
		FSMTest test = new FSMTest(0, null, null, -1);
		FSMTest aux;
		int size = 0;
		while(size < max_inputs) {
			for(Iterator<FSMTest> it = allTests.iterator(); it.hasNext(); ) {
				aux = it.next();
				if (test.getWeight() < aux.getWeight())
					test = aux;
			}
			if (size + test.getMaxLength() < max_inputs)
				tests.add(test);
			size += test.getMaxLength();
			allTests.remove(test);
		}
		
		
		Chromosome c = ChromosomeFactory.createChromosome(tests, max_inputs, maxPenalty);
		c.evaluate(testsVSmutantsAdd);
		v.updateResult(c);
		System.out.println(System.currentTimeMillis() - startTime);
	}
	
	public void randomSelection(String testsFile, String mutantsFile, int max_inputs, Window v) {
		long startTime = System.currentTimeMillis();
		
		Random rnd = new Random();
		List<FSMTest> allTests = ChromosomeFactory.readTests(testsFile);
		List<FSM> mutants = ChromosomeFactory.readMutants(mutantsFile);
		
		
		int[][] testsVSmutantsAdd = new int[allTests.size()][mutants.size()];
		int[][] testsVSmutantsMul = new int[allTests.size()][mutants.size()];
		int maxPenaltyAdd = 0;
		int maxPenaltyMul = 0;
		int minValue = 0;
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 0; mutant < mutants.size(); mutant++) {
				testsVSmutantsAdd[test][mutant] = allTests.get(test).killMutantAdd(mutants.get(mutant));
				if (testsVSmutantsAdd[test][mutant] > maxPenaltyAdd && testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					maxPenaltyAdd = testsVSmutantsAdd[test][mutant];
				if (testsVSmutantsAdd[test][mutant] < minValue)
					minValue = testsVSmutantsAdd[test][mutant];
			}
		}
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 0; mutant < mutants.size(); mutant++) {
				if (testsVSmutantsAdd[test][mutant] < Integer.MAX_VALUE)
					testsVSmutantsAdd[test][mutant] -= minValue;
			}
		}
		
		for(int test = 0; test < allTests.size(); test++) {
			for(int mutant = 0; mutant < mutants.size(); mutant++) {
				testsVSmutantsMul[test][mutant] = allTests.get(test).killMutantMul(mutants.get(mutant));
				if (testsVSmutantsMul[test][mutant] > maxPenaltyMul && testsVSmutantsMul[test][mutant] < Integer.MAX_VALUE)
					maxPenaltyMul = testsVSmutantsMul[test][mutant];
			}
		}
		
		int size = 0;
		List<FSMTest> tests = new ArrayList<FSMTest>();
		int test;
		while (size < max_inputs || rnd.nextDouble() < 0.75) {
			test = rnd.nextInt(allTests.size());
			if (allTests.get(test).getMaxLength() + size < max_inputs) {
				tests.add(allTests.get(test));
				size += allTests.get(test).getMaxLength();
			}
			else
				size = max_inputs + 1;
		}

		Chromosome cAdd = ChromosomeFactory.createChromosome(tests, max_inputs, maxPenaltyAdd);
		cAdd.evaluate(testsVSmutantsAdd);
		v.updateResult(cAdd);
		Chromosome cMul = ChromosomeFactory.createChromosome(tests, max_inputs, maxPenaltyMul);
		cMul.evaluate(testsVSmutantsMul);
		v.updateResult(cMul);

		
		System.out.println(System.currentTimeMillis() - startTime);
		
	}
	
	private Chromosome bestSubset(List<FSMTest> actualTest, int pos, List<List<FSMTest>> subsets, List<FSMTest> allTests, int max_inputs, int[][] testsVSmutants, int maxPenalty, int size) {
		if(pos < allTests.size()) {
			List<FSMTest> t = new ArrayList<FSMTest>(actualTest);
			Chromosome c1 = bestSubset(actualTest, pos + 1, subsets, allTests, max_inputs, testsVSmutants, maxPenalty, size);
			if(allTests.get(pos).getSize() + size <= max_inputs) {
				t.add(allTests.get(pos));
				size += allTests.get(pos).getSize();
				Chromosome c2 = bestSubset(t, pos + 1, subsets, allTests, max_inputs, testsVSmutants, maxPenalty, size);
				c1 = c1.getFitness() < c2.getFitness() ? c1 : c2;
			}
			return c1;
		}
		Chromosome c = new Chromosome(actualTest, max_inputs, maxPenalty);
		c.evaluate(testsVSmutants);
		return c;
	}

	private double media(Chromosome[] pob) {
		double m = 0;
		for(int i = 0; i < pob.length; i++)
			m += pob[i].getFitness();
		return m/pob.length;
	}
	
	private double peor(Chromosome[] pob) {
		Chromosome peor = pob[0];
		for (int i = 1; i < pob.length; i++)
			peor = pob[i].worse(peor);
		return peor.getFitness();
	}
	
	public double[] getMejores() {
		return mejores;
	}
	
	public double[] getMedias() {
		return medias;
	}
	
	public double[] getPeores() {
		return peores;
	}
	
	public Chromosome[] getMejoresAbsolutos() {
		return mejorHastaAhora;
	}
	
	public double[] getGeneraciones() {
		return generaciones;
	}
	
	private Chromosome score(Chromosome[] pob) {
		double total = 0;
		double acum = 0;
	
		double[] fitness = new double[pob.length];
		int[] posiciones = new int[pob.length];
		for(int i = 0; i < pob.length; i++){
			total += pob[i].getFitness();
			fitness[i] = pob[i].getFitness();
			posiciones[i] = i;
		}
		total *= 1.05;
		
		double punt_total = 0;
		for(int i = 0; i < pob.length; i++) {
			pob[i].setScore(total - pob[i].getFitness());
			punt_total += total - pob[i].getFitness();
		}
		
		quicksort(posiciones, fitness, 0, pob.length-1);

		for(int i = 0; i < pob.length; i++) {
			pob[i].setScore(pob[i].getScore()/punt_total);
			acum += pob[i].getScore();
			pob[i].setAccScore(acum);
			pob[posiciones[i]].setRank(i + 1);
		}

		return pob[posiciones[pob.length - 1]];
	}
	
	private void quicksort(int[] pos, double[] val, int low, int high) {
		if (low < high) {
			int p = partition(pos, val, low, high);
			quicksort(pos, val, low, p - 1);
        	quicksort(pos, val, p + 1, high);
		}
	}
	
	private void quicksort(int[][] pos, int low, int high) {
		if (low < high) {
			int p = partition(pos, low, high);
			quicksort(pos, low, p - 1);
        	quicksort(pos, p + 1, high);
		}
	}
	
	private int partition(int[] pos, double[] val, int low, int high) {
	    double pivot = val[high];
	    int aux;
	    double auxd;
	    
	    int i = low - 1;
	    for (int j = low; j < high; j++)
	        if (val[j] >= pivot) {
	            i++;
	            aux = pos[i];
	            pos[i] = pos[j];
	            pos[j] = aux;
	            auxd = val[i];
	            val[i] = val[j];
	            val[j] = auxd;
	        }
        aux = pos[i+1];
        pos[i+1] = pos[high];
        pos[high] = aux;
        auxd = val[i+1];
        val[i+1] = val[high];
        val[high] = auxd;
	    return i + 1;
	}
	
	private int partition(int[][] pos, int low, int high) {
		int[] pivot = pos[high];
		int[] aux;
		
		int i = low - 1;
		for(int j = low; j < high; j++) {
			int pivotVal = 0, lineVal = 0;
			for(int k = 1; k < pivot.length; k++) {
				pivotVal += pivot[k];
				lineVal += pos[j][k];
			}
			if(lineVal >= pivotVal) {
				i++;
				aux = pos[i];
				pos[i] = pos[j];
				pos[j] = aux;
			}
		}
		aux = pos[i+1];
		pos[i+1] = pos[high];
		pos[high] = aux;
		
		return i+1;
	}
	
	private final class Hebra extends Thread {
		private Chromosome mejor;
		private int i;
		private double[] generacion;
		private double[] mejores;
		private double[] peores;
		private double[] medias;	
		private double[] absolutos;
		
		public Hebra(Chromosome c, int i) {
			super();
			mejor = c;
			i = i+2;		//por cómo se inicializa todo, debe ser así
			this.i = i;
			double[] g = Controller.this.getGeneraciones();
			double[] m = Controller.this.getMejores();
			double[] p = Controller.this.getPeores();
			double[] a = Controller.this.getMedias();
			Chromosome[] croms = Controller.this.getMejoresAbsolutos();
			generacion = new double[i];
			mejores = new double[i];
			peores = new double[i];
			medias = new double[i];
			absolutos = new double[i];
			for(int j = 0; j < i; j++){
				generacion[j] = g[j];
				mejores[j] = m[j];
				peores[j] = p[j];
				medias[j] = a[j];
				absolutos[j] = croms[j].getFitness();
			}
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(10);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						v.updateResult(mejor);
						v.refreshGraph(generacion, mejores, peores, medias, absolutos);
						v.refreshGraph(generacion, mejores, peores, medias, absolutos);
					}
				});
			} catch (InterruptedException e) {
				System.err.println("Matando Hebra de controlador "+i);
			}
		}
	}
}
