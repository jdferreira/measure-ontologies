package net.jdferreira.ontologies.measure;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class App {

	public static void main(String[] args) throws OWLOntologyCreationException {

		String filename = "ontologies/human.owl";
		File file = new File(filename);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

		long countClasses = 0;

		long sumChildren = 0;
		long maxChildren = 0;
		long countLeaves = 0;
		long countWithOneChild = 0;
		long countWithManyChildren = 0;

		ArrayList<OWLClass> roots = new ArrayList<>();

		Iterator<OWLClass> iterator = ontology.classesInSignature().iterator();
		while (iterator.hasNext()) {
			OWLClass owlClass = iterator.next();
			countClasses++;

			long countParents = ontology.subClassAxiomsForSubClass(owlClass).count();
			long countChildren = ontology.subClassAxiomsForSuperClass(owlClass).count();

			sumChildren += countChildren;

			if (countChildren == 0)
				countLeaves++;
			
			else if (countChildren == 1)
				countWithOneChild++;
			
			else if (countChildren >= 25)
				countWithManyChildren++;

			if (countChildren > maxChildren)
				maxChildren = countChildren;
			
			if (countParents == 0)
				roots.add(owlClass);
		}

		Optional<Integer> maxDepth = roots.stream().map((owlClass) -> getDepth(ontology, owlClass))
				.max(Integer::compare);

		System.out.println(filename);
		
		System.out.printf("#classes total: %d\n", countClasses);
		
		double avg = ((double) sumChildren) / countClasses;
		System.out.printf("Average #children: %.2f\n", avg);
		
		System.out.printf("Maximum #children: %d\n", maxChildren);
		
		System.out.printf("#classes with 0 children: %d\n", countLeaves);
		
		System.out.printf("#classes with 1 child: %d\n", countWithOneChild);
		
		System.out.printf("#classes with 25+ children: %d\n", countWithManyChildren);
		
		System.out.printf("#roots (no parent): %d\n", roots.size());
		
		System.out.printf("Maximum depth: %d\n", maxDepth.get().intValue());

	}

	private static int getDepth(OWLOntology ontology, OWLClass owlClass) {
		Optional<Integer> maxChildrenDepth = //
				ontology.subClassAxiomsForSuperClass(owlClass) //
						.filter((axiom) -> axiom.getSubClass().isOWLClass()) //
						.map((axiom) -> axiom.getSubClass().asOWLClass()) //
						.map((cls) -> getDepth(ontology, cls) + 1) //
						.max(Integer::compare);

		return maxChildrenDepth.orElse(1);
	}
}
