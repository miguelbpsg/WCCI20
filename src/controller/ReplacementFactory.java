package controller;

import model.replacement.Replacement;
import model.replacement.DirectReplacement;
import model.replacement.ElitistReplacement;

public class ReplacementFactory {

	public static Replacement createReplacement(Replacement r, double elit, int heur, int[][] testsVSmutants) {
		switch(r.getReplacement()) {
		case 1:
			return new DirectReplacement(heur, testsVSmutants);
		case 2:
			return new ElitistReplacement(elit, heur, testsVSmutants);
		default:
			System.err.println("Replacement Factory Error");
			return null;
		}
	}
}
