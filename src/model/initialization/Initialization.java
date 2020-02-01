package model.initialization;

import java.util.List;
import java.util.Random;

import model.chromosome.Chromosome;
import model.chromosome.FSM;
import model.chromosome.FSMTest;

public abstract class Initialization {
	protected int initialization;
	protected int size;
	protected Random rnd = new Random();

	public abstract Chromosome[] initialize(int size_pob, int heur, List<FSMTest> allTests, List<FSM> mutants);
	public void setSize(int size) {
		this.size = size;
	}
	public int getInitialization() {
		return initialization;
	}
	
}
