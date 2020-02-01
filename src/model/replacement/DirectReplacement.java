package model.replacement;

import model.chromosome.Chromosome;

public class DirectReplacement extends Replacement {

	public DirectReplacement(int heur, int[][] testsVSmutants) {
		this.replacement = 1;
		this.heur = heur;
		this.testsVSmutants = testsVSmutants;
	}

	@Override
	public Chromosome[] replace(Chromosome[] pob, Chromosome[] new_pob) {
		for (int i = 0; i < new_pob.length; i++) {
			if(heur == 0)
				new_pob[i].evaluate(testsVSmutants);
			else
				new_pob[i].evaluate(testsVSmutants);
		}
		return new_pob;
	}

	@Override
	public String toString() {
		return "Direct replacement";
	}
}
