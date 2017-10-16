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

		String filename = "ontologies/mouse.owl";
		File file = new File(filename);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

		long countChildren = 0;
		long countClasses = 0;

		ArrayList<OWLClass> roots = new ArrayList<>();

		Iterator<OWLClass> iterator = ontology.classesInSignature().iterator();
		while (iterator.hasNext()) {
			OWLClass owlClass = iterator.next();
			countChildren += ontology.subClassAxiomsForSuperClass(owlClass).count();
			countClasses++;

			long countParents = ontology.subClassAxiomsForSubClass(owlClass).count();
			if (countParents == 0)
				roots.add(owlClass);
		}

		double avg = ((double) countChildren) / countClasses;
		Optional<Integer> maxDepth = roots.stream().map((owlClass) -> getDepth(ontology, owlClass))
				.max(Integer::compare);

		System.out.printf("Average #children: %.2f\n", avg);
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
