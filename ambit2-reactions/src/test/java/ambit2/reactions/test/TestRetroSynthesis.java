package ambit2.reactions.test;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;

import ambit2.reactions.retrosynth.RetroSynthesis;
import ambit2.reactions.retrosynth.RetroSynthesisResult;
import ambit2.smarts.SmartsHelper;

public class TestRetroSynthesis {
	RetroSynthesis retroSyn;

	public static void main(String[] args) throws Exception {

		TestRetroSynthesis trs = new TestRetroSynthesis();
		trs.retroSyn = new RetroSynthesis();
		// System.out.println("Retro Synthesis Knowledge base:\n" +
		// trs.retroSyn.getReactionKnowledgeBase().toString());

		// trs.test("CCNS");
		trs.testSmi("c1[n+](S)ccn1O");
	}

	@Test
	public void test() throws Exception {
		retroSyn = new RetroSynthesis();
		// System.out.println("Retro Synthesis Knowledge base:\n" +
		// trs.retroSyn.getReactionKnowledgeBase().toString());

		// trs.test("CCNS");
		testSmi("c1[n+](S)ccn1O");
	}

	public void testSmi(String smi) throws Exception {
		System.out.println("Testing Retro Synthesis for " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		// System.out.println("Atom attributes: \n" +
		// SmartsHelper.getAtomsAttributes(mol));

		System.out.println();
		retroSyn.setStructure(mol);
		RetroSynthesisResult result = retroSyn.run();

		System.out.println("Retro Synthesis result:\n" + result.toString());
	}

}
