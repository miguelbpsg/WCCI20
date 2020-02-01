package model.chromosome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FSM {
	private List<Node> nodes;
	private List<Transition> transitions;
	private Set<String> inputs;
	private Set<String> outputs;
	private Node initialNode;
	
	public FSM(List<Node> nodes, List<Transition> transitions, Set<String> inputs, Set<String> outputs, Node initialNode) {
		this.nodes = nodes;
		this.transitions = transitions;
		this.inputs = inputs;
		this.outputs = outputs;
		this.initialNode = initialNode;
	}
	
	public FSM(FSM m) {
		this.nodes = new ArrayList<Node>(m.getNodes());
		this.transitions = new ArrayList<Transition>(m.getTransitions());
		this.inputs = new HashSet<String>(m.getInputs());
		this.outputs = new HashSet<String>(m.getOutputs());
		this.initialNode = m.getInitialNode();
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public List<Transition> getTransitions() {
		return transitions;
	}
	
	public Set<String> getInputs() {
		return inputs;
	}

	public Set<String> getOutputs() {
		return outputs;
	}

	public Node getInitialNode() {
		return initialNode;
	}
	
	public List<FSM> generateAllMutants(){
		List<FSM> mutants = new ArrayList<FSM>();
		for(Transition t : transitions) {
			Iterator<String> iterator = outputs.iterator();
			String o = iterator.next();
			while(iterator.hasNext() && t.getOutput() == o)
				o = iterator.next();
			List<Transition> transAux = new ArrayList<Transition>(transitions);
			transAux.remove(t);
			
			List<Node> nodesAux = new ArrayList<Node>(nodes);
			nodesAux.remove(t.getSource());
			Node newSource = new Node(t.getSource());
			newSource.removeTransition(t);
			nodesAux.add(newSource);
			Transition newTransition = new Transition(newSource, t.getTarget(), t.getInput(), o);
			newSource.addTransition(newTransition);
			
			transAux.add(newTransition);
			FSM mutant = new FSM(nodesAux, transAux, inputs, outputs, initialNode);
			mutants.add(mutant);
			
			for(Node n : nodes) {//changing all targets
				if(!t.getTarget().equals(n)) {
					transAux = new ArrayList<Transition>(transitions);
					transAux.remove(t);
					
					nodesAux = new ArrayList<Node>(nodes);
					nodesAux.remove(t.getSource());
					newSource = new Node(t.getSource());
					newSource.removeTransition(t);
					nodesAux.add(newSource);
					newTransition = new Transition(newSource, n, t.getInput(), t.getOutput());
					newSource.addTransition(newTransition);
					
					transAux.add(newTransition);
					mutant = new FSM(nodes, transAux, inputs, outputs, initialNode);
					mutants.add(mutant);
				}
			}
		}
		return mutants;
	}
	
	public List<FSM> generateMutants(double ratio){
		Random rnd = new Random();
		List<FSM> mutants = new ArrayList<FSM>();
		for(Transition t : transitions) {
			Iterator<String> iterator = outputs.iterator();
			String o = iterator.next();
			while(iterator.hasNext() && t.getOutput() == o)
				o = iterator.next();
			if (rnd.nextDouble() <= ratio) {
				List<Transition> transAux = new ArrayList<Transition>(transitions);
				transAux.remove(t);
				
				List<Node> nodesAux = new ArrayList<Node>(nodes);
				nodesAux.remove(t.getSource());
				Node newSource = new Node(t.getSource());
				newSource.removeTransition(t);
				nodesAux.add(newSource);
				Transition newTransition = new Transition(newSource, t.getTarget(), t.getInput(), o);
				newSource.addTransition(newTransition);
				
				transAux.add(newTransition);
				FSM mutant = new FSM(nodesAux, transAux, inputs, outputs, initialNode);
				mutants.add(mutant);
			}
			
			for(Node n : nodes) {//changing all targets
				if(!t.getTarget().equals(n) && rnd.nextDouble() <= ratio) {
					List<Transition> transAux = new ArrayList<Transition>(transitions);
					transAux.remove(t);
					
					List<Node> nodesAux = new ArrayList<Node>(nodes);
					nodesAux.remove(t.getSource());
					Node newSource = new Node(t.getSource());
					newSource.removeTransition(t);
					nodesAux.add(newSource);
					Transition newTransition = new Transition(newSource, n, t.getInput(), t.getOutput());
					newSource.addTransition(newTransition);
					
					transAux.add(newTransition);
					FSM mutant = new FSM(nodes, transAux, inputs, outputs, initialNode);
					mutants.add(mutant);
				}
			}
		}
		return mutants;
	}
		
	private static FSM generateRandomSpecification(int numNodes, int numInputs, int numOutputs) {
		Random rnd = new Random();
		List<Node> nodes = new ArrayList<Node>();
		List<Transition> transitions = new ArrayList<Transition>();
		Set<String> inputset = new HashSet<String>();
		Set<String> outputset = new HashSet<String>();
		
		for(int i = 0; i < numNodes; i++)
			nodes.add(new Node(i));
		for(int i = 0; i < numInputs; i++)
			inputset.add("" + i);
		for(int i = 0; i < numOutputs; i++)
			outputset.add("" + i);
		
		for(int node = 0; node < numNodes; node++) {
			for(String input : inputset) {
				Transition t = new Transition(nodes.get(node),
					nodes.get(rnd.nextInt(numNodes)),
					input,
					"" + rnd.nextInt(numOutputs));
				nodes.get(node).addTransition(t);
				transitions.add(t);
			}
		}
		
		return new FSM(nodes,transitions,inputset,outputset,nodes.get(0));
	}
	
	
	public static List<FSM> generateSepcifications(int numSpec, int minNodes, int maxNodes, int minInputs, int maxInputs, int minOutputs, int maxOutputs) {
		List<FSM> specifications = new ArrayList<FSM>();
		Random rnd = new Random();
		
		for(int i = 0; i < numSpec; i++) {
			specifications.add(generateRandomSpecification(
					rnd.nextInt(maxNodes - minNodes + 1) + minNodes,
					rnd.nextInt(maxInputs - minInputs + 1) + minInputs,
					rnd.nextInt(maxOutputs - minOutputs + 1) + minOutputs
					));
		}
		return specifications;
	}
	
	public List<FSMTest> generateTests(int profMax, int numTests) {
		List<FSMTest> tests = new ArrayList<FSMTest>();
		Random rnd = new Random();
		for(int numTest = 0; numTest < numTests; numTest++) {
			int profTest = 0;
			List<String> ins = new ArrayList<String>();
			List<String> outs = new ArrayList<String>();
			Node actualNode = initialNode;
			do {
				Transition tran = actualNode.getTransition(rnd.nextInt(inputs.size()));
				ins.add(tran.getInput());
				outs.add(tran.getOutput());
				actualNode = tran.getTarget();
				profTest++;
			} while (rnd.nextDouble() > 0.75/profMax && profTest < profMax);
			tests.add(new FSMTest(numTest, ins, outs, rnd.nextInt(10)+1));
		}
		return tests;
	}

	public String toFile() {
		String file = nodes.size() + "\n" + transitions.size() + "\n";
		for(int i = 0; i < transitions.size(); i++) {
			file += transitions.get(i).toString();
			file += i == transitions.size() - 1 ? "" : "\n";
		}
		return file;
	}
	
	public String toString() {
		return initialNode.toString() + "\t" + transitions.toString();
	}
}
