package controller;

import java.util.List;

import model.mutation.Mutation;
import model.mutation.ReplaceMutation;
import model.chromosome.FSMTest;
import model.mutation.ExtraTestMutation;

public class MutationFactory {

	public static Mutation createMutation(Mutation m, double prob, List<FSMTest> tests) {
		switch(m.getMutation()) {
		case 1:
			return new ExtraTestMutation(prob, tests);
		case 2:
			return new ReplaceMutation(prob, tests);
		default:
			System.err.println("Mutation Factory Error");
			return null;
		}
	}
}
