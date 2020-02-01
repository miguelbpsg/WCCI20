package model.replacement;

import model.chromosome.Chromosome;

public abstract class Replacement {
	protected int replacement;
	protected int heur;
	protected int[][] testsVSmutants;
	
    public abstract Chromosome[] replace(Chromosome[] pob, Chromosome[] new_pob);
    
    public int getReplacement() {
    	return replacement;
    }
}
