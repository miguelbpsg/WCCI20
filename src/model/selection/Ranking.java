package model.selection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import controller.ChromosomeFactory;
import model.chromosome.Chromosome;

public class Ranking implements Selection {
	private Random Rnd = new Random();
	
	@Override
	public Chromosome[] select(Chromosome[] pob, int tam_pob) {
		double prob;
		int pos_super;
		int totalRank = 0;
		for(int i = 0; i < tam_pob; i++) {
			totalRank += pob[i].getRank();
		}
			
		int rankParcial;
		Chromosome[] new_pob = new Chromosome[tam_pob];


		List<Chromosome> aux = Arrays.asList(pob);
		Collections.shuffle(aux);
		
		for(int i = 0; i < tam_pob; i++) {
			prob = Rnd.nextDouble();
			pos_super = 0;
			rankParcial = aux.get(pos_super).getRank();
			while(prob > rankParcial / totalRank){
				pos_super++;
				rankParcial += aux.get(pos_super).getRank();
			}
			new_pob[i] = ChromosomeFactory.copyChromosome(aux.get(pos_super));
		}
		return new_pob;
	}

	@Override
	public String toString() {
		return "Ranking";
	}
	
	@Override
	public int getSelection() {
		return 1;
	}
}
