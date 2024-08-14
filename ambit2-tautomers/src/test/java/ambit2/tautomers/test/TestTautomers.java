package ambit2.tautomers.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import ambit2.base.exceptions.AmbitIOException;
import ambit2.core.data.MoleculeTools;
import ambit2.core.helper.CDKHueckelAromaticityDetector;
import ambit2.core.io.FileInputState;
import ambit2.core.processors.structure.HydrogenAdderProcessor;
import ambit2.smarts.ChemObjectFactory;
import ambit2.smarts.IsomorphismTester;
import ambit2.smarts.SmartsHelper;
import ambit2.smarts.SmartsParser;
import ambit2.smarts.StereoChemUtils;
import ambit2.tautomers.CACTVSRanking;
import ambit2.tautomers.KnowledgeBase;
import ambit2.tautomers.TautomerConst;
import ambit2.tautomers.TautomerConst.GAT;
import ambit2.tautomers.TautomerManager;
import ambit2.tautomers.TautomerRanking;
import ambit2.tautomers.processor.StructureStandardizer;
import ambit2.tautomers.ranking.EnergyRanking;
import ambit2.tautomers.ranking.TautomerStringCode;
import ambit2.tautomers.rules.EnergyRule;
import ambit2.tautomers.rules.JsonRuleParser;

public class TestTautomers {
	public static Logger logger = Logger.getLogger(TestTautomers.class.getName());
	public TautomerManager tman = new TautomerManager();
	public TautomerRanking tautomerRanking = new TautomerRanking();
	public InChITautomerGenerator itg = new InChITautomerGenerator();
	// public RuleStructureFactory rsf = new RuleStructureFactory();
	public ChemObjectFactory cof = new ChemObjectFactory(SilentChemObjectBuilder.getInstance());
	public boolean FlagExplicitHydrogens = false;
	public StructureStandardizer standardprocessor = new StructureStandardizer();
	public boolean FlagPrintMoleculeToSmilesExeptions = false;

	public static void main(String[] args) throws Exception {
		TestTautomers tt = new TestTautomers();
		tt.tman.setEnergyRanking(new EnergyRanking()); // This is done here in
														// order to catch the
														// exception from the
														// energy-rule parsing!
		tt.tman.tautomerFilter.setFlagApplyWarningFilter(true);
		tt.tman.tautomerFilter.setFlagApplyExcludeFilter(true);
		tt.tman.tautomerFilter.setFlagApplyDuplicationFilter(true);
		tt.tman.tautomerFilter.setFlagApplyDuplicationCheckIsomorphism(false);
		tt.tman.tautomerFilter.setFlagApplyDuplicationCheckInChI(true);
		tt.tman.tautomerFilter.setFlagFilterIncorrectValencySumStructures(true);
		tt.tman.tautomerFilter.FlagApplySimpleAromaticityRankCorrection = true;

		tt.tman.FlagSetStereoElementsOnTautomerProcess = true;

		tt.tman.FlagNewRuleInstanceSearchOnEnergyRanking = false;
		tt.tman.FlagCheckDuplicationOnRegistering = true;

		tt.tman.FlagRecurseBackResultTautomers = false;

		tt.tman.FlagPrintTargetMoleculeInfo = true;
		tt.tman.FlagPrintExtendedRuleInstances = true;
		tt.tman.FlagPrintIcrementalStepDebugInfo = false;

		tt.tman.getKnowledgeBase().activateChlorineRules(false);
		tt.tman.getKnowledgeBase().activateRingChainRules(false);
		// tt.tman.getKnowledgeBase().use13ShiftRulesOnly(true);
		tt.tman.getKnowledgeBase().use15ShiftRules(true);
		tt.tman.getKnowledgeBase().use17ShiftRules(false);

		// tt.tman.maxNumOfBackTracks = 1000;
		// tt.tman.maxNumOfTautomerRegistrations = 1000;

		tt.tman.FlagProcessRemainingStackIncSteps = true;
		tt.tman.FlagCalculateCACTVSEnergyRank = true;

		tt.tman.FlagRegisterOnlyBestRankTautomers = false;
		// tt.tman.setCanonicTautomerMethod(CanonicTautomerMethod.CACTVS_RANK_INCHI_KEY);
		// tt.tman.setCanonicTautomerMethod(CanonicTautomerMethod.ENERGY_RANK_INCHI_KEY);

		// tt.performTestCases();

		// tt.visualTest("OC=CCC(CC=CO)CCCC=O");
		// tt.test("O=CC(C)([H])C"); --> problem with the explicit H atoms
		// tt.test("OC=NCC(CC=CO)CCCC");
		// tt.test("OC=CCCNC=O");
		// tt.testCase("OC=CCCNC=O", new String[]{"OC=CCCNC=O", "OC=CCCN=CO",
		// "O=CCCCNC=O", "O=CCCCN=CO"} , true);
		// tt.testCloning("CC(C)C");
		// tt.test("C=C(O)N");
		// tt.test("NN=CO");
		// tt.visualTest("N=C(N)NC=O");
		// tt.visualTest("NC(C)=N");
		// tt.visualTest("C1=CN=C(N)NC1=O");
		// tt.visualTest("OC=1C=CC=CC=1"); //Kekule aromatic - !!!!

		// tt.visualTestInChI("o1cccc1");
		// tt.visualTestInChI("O1C=CC=C1");
		// tt.visualTestInChI("O=CCC");
		// tt.visualTestInChI("NC1=CC(N)=NC(O)=N1");

		// tt.visualTest("NC1=CC(N)=NC(O)=N1");
		// tt.visualTest("OC1=C(O)C=CC=C1");
		// tt.visualTest("CC(=O)C");
		// tt.visualTest("N=NNCCC");
		// tt.visualTest("S=CNCC");

		// tt.visualTest("N1=NC=CC=N1");
		// tt.visualTest("N1=CC=CN=C1");

		// tt.visualTest("O=C1C=CC(=CC)CC1");

		// tt.visualTest("SC1=CC=CC=C1");

		// tt.visualTest("CS#CO");
		// tt.visualTest("S=N1CC=CC=C1");

		// tetracyclin
		// tt.visualTest("CN(C)C1=C(O)C(C(N)=O)=C(O)C2(O)C1CC1C(=C2O)C(=O)C2=C(C=CC=C2O)C1(C)O");
		// tt.visualTest("CN(C)C1=C(O)C(C(N)=O)=C(O)C2(O)C1CC1C(=C2O)C(=O)C2=C(C=CC=C2O)C1(C)O");

		// tt.test("[H][C@@]12CCCN1C(=O)[C@H](NC(=O)[C@@H](NC(=O)C1=C3N=C4C(OC3=C(C)C=C1)=C(C)"
		// +
		// "C(=O)C(N)=C4C(=O)N[C@H]1[C@@H](C)OC(=O)[C@H](C(C)C)N(C)C(=O)CN(C)C(=O)[C@]3([H])CCCN3C(=O)[C@H](NC1=O)C(C)C)",
		// TautomerConst.GAT_Comb_Pure);

		// tt.visualTest("OC1=CC(=CC(O)=C1O)C(=O)OC1=CC(=CC(O)=C1O)C(=O)OC[C@H]1O[C@@H](OC(=O)C2=CC(O)=C(O)C(OC(=O)C3=CC(O)=C(O)"
		// +
		// "C(O)=C3)=C2)[C@H](OC(=O)C2=CC(O)=C(O)C(OC(=O)C3=CC(O)=C(O)C(O)=C3)=C2)[C@@H](OC(=O)C2=CC(O)=C(O)"
		// +
		// "C(OC(=O)C3=CC(O)=C(O)C(O)=C3)=C2)[C@@H]1OC(=O)C1=CC(O)=C(O)C(OC(=O)C2=CC(O)=C(O)C(O)=C2)=C1"/*,
		// TautomerConst.GAT_Incremental*/);

		// tt.visualTest("OC1=CC(=CC(O)=C1O)C(=O)OC1=CC(=CC(O)=C1O)C(=O)OC[C@H]1O[C@@H](OC(=O)C2=CC(O)=C(O)C(OC(=O)C3=CC(O)=C(O)"
		// +
		// "C(O)=C3)=C2)[C@H](OC(=O)C2=CC(O)=C(O)C(OC(=O)C3=CC(O)=C(O)C(O)=C3)=C2)[C@@H](OC(=O)C2=CC(O)=C(O)"
		// +
		// "C(OC(=O)C3=CC(O)=C(O)C(O)=C3)=C2)[C@@H]1OC(=O)C1=CC(O)=C(O)C(OC(=O)C2=CC(O)=C(O)C(O)=C2)=C1",
		// TautomerConst.GAT_Comb_Improved);

		// tt.visualTest("S=N1CC=CC=C1");

		// tt.visualTest("O1=CC=CN=C1");

		// tt.visualTest("OC=1N=CN=CC=1"); //Kekule aromatic - !!!!

		// tt.tman.FlagEnergyRankingMethod = TautomerConst.ERM_NEW;
		tt.tman.FlagNewRuleInstanceSearchOnEnergyRanking = false;
		tt.tman.FlagRegisterOnlyBestRankTautomers = false;
		// tt.visualTest("[H][C@]12C[C@@]3([H])OC(=O)\\C=C/C=C/C(O[C@H](O)CC(C)CC(=O)OC[C@@]4(C[C@@]([H])(O)[C@H](C)C[C@@]4([H])O1)[C@]3(C)C21CO1)C(C)O");

		tt.standardprocessor.setGenerateTautomers(true);

		// tt.test(
		// tt.visualTest(
		// tt.testStereoInfo(
		// tt.testStandardizer(
		// "S1C(N(\\C(=C/2\\C3=C(C(C(C([H])([H])[H])(C([H])([H])[H])[H])"
		// +
		// "=C(O[H])C2=O)C(=C(C(C=4C(=C(C5=C(C4[H])/C(/C(=O)C(O[H])=C5C(C([H])([H])[H])(C([H])([H])[H])[H])"
		// +
		// "=C(/N(C=6SC(=C(N6)C7=C(C(=C(C(=C7[H])[H])[H])[H])[H])[H])[H])\\[H])[H])C([H])([H])[H])"
		// +
		// "=C3[H])C([H])([H])[H])[H])\\[H])[H])=NC(=C1[H])C8=C(C(=C(C(=C8[H])[H])[H])[H])[H]");

		// tt.visualTest("CC1=CC2=C(/C(=C/NC3=NC(=CS3)C4=CC=CC=C4)/C(=O)C(=C2C(C)C)O)C=C1C5=CC\\6=C(C(=C(C(=O)/C6=C\\NC7=NC(=CS7)C8=CC=CC=C8)O)C(C)C)C=C5C");

		// tt.visualTest("C1=CN=C(N)NC1(=O)");

		// tt.tman.getKnowledgeBase().activateRule("O=SC", false);
		tt.tman.FlagAddImplicitHAtomsOnTautomerProcess = false;
		// tt.visualTest("BrC1=CC=C(C=C1)S(=O)(=O)CC1=CC=C(O1)C(=O)N1CCOCC1");
		// tt.visualTest("ClC=1C=C(C2=NN(C(=C2C3=CC=C(N(O)=O)C=C3)C(=O)N4C(OCC4)=O)C5=CC=CC=C5)C=CC1");
		// tt.visualTest("O(C=1C(NC(=O)C=NN#N)=CC(OC)=CC1)C");
		// tt.visualTest("COC1=CC(=C(C=C1)OC)NC(=O)CN=[N+]=[N-]");
		// tt.visualTest("COC1=CC(=C(C=C1)OC)NC(=O)CN=N#N");
		tt.visualTest("CC1CN(CC(O1)C)C2=NC3=CC=CC=C3N=C2C(C#N)S(=O)(=O)C4=CC=CC(=C4)C");

		// tt.visualTest("OC(=O)CCC=CN");

		// tt.visualTest("O=C1OC3=CC=CC=C3(C(O)C1C(C=2C=CC=CC=2)CC(=O)C)");
		// tt.visualTest("C3=C(C(C1=C(OC2=C(C1=O)C=CC=C2)O)CC(=O)C)C=CC=C3");

		// tt.visualTest("O=C1OC3=CC=CC=C3(C(O)C1C(C=2C=CC=CC=2)CC(=O)C)");
		// //from Kekulizer

		// tt.visualTest("O=C1[N+](=N)CN=CC1");
		// tt.visualTest("OC1=CC=CC=C1");

		// tt.visualTest("NC1=CC=CC=C1");
		// tt.visualTest("N=C1C=CC=CC1");
		// tt.visualTest("N=C1C=CC=CC1",TautomerConst.GAT_Comb_Pure);

		// tt.visualTest("N=C(O)C=CN"); //two problems (1) alene atoms are
		// obtained, (2) missing tautomers

		// tt.visualTest("OC=C(N)CCC(N)=CN",TautomerConst.GAT_Comb_Pure);
		// tt.visualTest("OC=1C=CC(=CC=1(NCS(=O)(=O)O))[As]=[Bi][As](C=2C=CC(O)=C(C=2)NCS(=O)(=O)O)[Bi]=[As]C=3C=CC(O)=C(C=3)NCS(=O)(=O)O",TautomerConst.GAT_Comb_Improved);
		// tt.visualTest("OC=1C=CC(=CC=1(NCS(=O)(=O)O))[As]=[Bi][As](C=2C=CC(O)=C(C=2)NCS(=O)(=O)O)[Bi]=[As]C=3C=CC(O)=C(C=3)NCS(=O)(=O)O");

		// tt.visualTest("O=C(SO)N",TautomerConst.GAT_Incremental);

		// tt.visualTest("NC1=CC=CC2=C(O)N=NC(O)=C12", 1);

		// tt.visualTest("O=C(N)C",0);

		// tt.testAdenine();

		// tt.testInChIGenerator("C=CCCC");
		// tt.testInChIGenerator("CCCC=C");

		// tt.testInChIGenerator("C1=CC=CC=C1");
		// tt.testInChIGenerator("c1ccccc1");

		// tt.testInChIGenerator("N=1C=OC=CC=1");
		// tt.testInChIGenerator("N1=CO=CC=C1");

		// tt.testConnectStructures("C1CN1",2,"CCl",0, IBond.Order.SINGLE);
		// tt.testCondenseStructures("C1CN1",0,1,"BrCCCl",1,2);

		// tt.testRuleActivation(new String[] {"keto/enol","amin/imin"});

		// tt.tman.getKnowledgeBase().activateRule("keto/enol", true);
		// tt.FlagExplicitHydrogens = true;
		// tt.tman.getKnowledgeBase().activateChlorineRules(true);
		// tt.visualTest("O=CC(C)(C)Cl");

		// tt.FlagExplicitHydrogens = false;

		// tt.testStereoInfo("O=C[C@](C)(Cl)Br");

		// RuleStructureFactory rsf = new RuleStructureFactory();
		// rsf.makeRuleStrcutures("CC=O", 0, "C=CO", 0, "/test.smi",
		// "/gen-test.txt");
		// rsf.calculateEnergyDifferences("/gen-test.txt",
		// "/energies-diff-test.txt");

		// tt.visualTestFromFile("/work/tempAmbitIn.sdf");

		// tt.testCACTVSRank("c1ccccc1CCC=O");

		// tt.testEnergyRules();
	}

	public void performTestCases() throws Exception {
		int nErrors = 0;

		nErrors += testCase("OC=CCCNC=O", new String[] { "OC=CCCNC=O", "OC=CCCN=CO", "O=CCCCNC=O", "O=CCCCN=CO" },
				false, tman);

		logger.log(Level.WARNING, "Errors: " + nErrors);

	}

	public void test0(String smi) throws Exception {
		logger.log(Level.INFO, "Testing0(combinatorial aproach)0: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		tman.setStructure(mol);
		List<IAtomContainer> resultTautomers = tman.generateTautomers();
	}

	public void test(String smi) throws Exception {

		logger.log(Level.INFO, "Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		tman.setStructure(mol);

		// List<IAtomContainer> resultTautomers = tman.generateTautomers();

		List<IAtomContainer> resultTautomers = tman.generateTautomersIncrementaly();
		for (int i = 0; i < resultTautomers.size(); i++) {
			System.out.println(StereoChemUtils.getStereoElementsStatus(resultTautomers.get(i)));
			System.out.println("   " + moleculeToSMILES(resultTautomers.get(i), false));
		}

	}

	public void test(String smi, GAT algorithmType) throws Exception {
		logger.log(Level.INFO, "Algorithm type " + algorithmType + "   Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi, FlagExplicitHydrogens);

		tman.setStructure(mol);
		List<IAtomContainer> resultTautomers;

		switch (algorithmType) {
		case Comb_Pure:
			resultTautomers = tman.generateTautomers();
			break;

		case Incremental:
			resultTautomers = tman.generateTautomersIncrementaly();
			break;

		case Comb_Improved:
			resultTautomers = tman.generateTautomers_ImprovedCombApproach();
			break;

		default:
			System.out.println("Unsupported algorithm type!");
			return;
		}

		tman.printDebugInfo();

		logger.log(Level.INFO, "\n  Result tautomers: ");
		List<IAtomContainer> v = new ArrayList<IAtomContainer>();

		for (int i = 0; i < resultTautomers.size(); i++) {
			Double rank = (Double) resultTautomers.get(i).getProperty(TautomerConst.TAUTOMER_RANK);
			if (rank == null)
				rank = new Double(999999);

			Double csRank = (Double) resultTautomers.get(i).getProperty(TautomerConst.CACTVS_ENERGY_RANK);
			if (csRank == null)
				csRank = new Double(999999);

			logger.log(Level.INFO, "   " + rank.toString() + "   CS_RANK = " + csRank.toString() + "   "
					+ moleculeToSMILES(resultTautomers.get(i), false));
			v.add(resultTautomers.get(i));
		}

		logger.log(Level.WARNING, "Generated: " + resultTautomers.size() + " tautomers.");

	}

	public void visualTest(String smi) throws Exception {
		logger.log(Level.INFO, "Visual Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi, FlagExplicitHydrogens);

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		CDKHueckelAromaticityDetector.detectAromaticity(mol);
		// implicit H count is NULL if read from InChI ...
		mol = AtomContainerManipulator.removeHydrogens(mol);
		CDKHydrogenAdder.getInstance(mol.getBuilder()).addImplicitHydrogens(mol);

		if (FlagExplicitHydrogens)
			HydrogenAdderProcessor.convertImplicitToExplicitHydrogens(mol);

		tman.setStructure(mol);
		// List<IAtomContainer> resultTautomers = tman.generateTautomers();

		List<IAtomContainer> resultTautomers = tman.generateTautomersIncrementaly();
		double distr[] = tautomerRanking.getProbabilityDistribution(resultTautomers);
		tman.printDebugInfo();

		logger.log(Level.INFO, "\n  Result tautomers: ");
		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol);
		v.add(null);
		for (int i = 0; i < resultTautomers.size(); i++) {
			Double rank = (Double) resultTautomers.get(i).getProperty(TautomerConst.TAUTOMER_RANK);
			if (rank == null)
				rank = new Double(999999);

			Double csRank = (Double) resultTautomers.get(i).getProperty(TautomerConst.CACTVS_ENERGY_RANK);
			if (csRank == null)
				csRank = new Double(999999);

			logger.log(Level.INFO,
					TautomerStringCode.getCode(resultTautomers.get(i), false, tman.getCodeStrBondSequence()) + "   "
							+ rank.toString() + "    " + (100.0 * distr[i]) + "%    CS_RANK = " + csRank.toString()
							+ "    " + moleculeToSMILES(resultTautomers.get(i), false));
			v.add(resultTautomers.get(i));
		}

		logger.log(Level.INFO, "Generated: " + resultTautomers.size() + " tautomers.");

		// preProcessStructures(v);
		TestStrVisualizer tsv = new TestStrVisualizer(v);

	}

	public void visualTest(String smi, GAT algorithmType) throws Exception {
		logger.log(Level.INFO, "Algorithm type " + algorithmType + "   Visual Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi, FlagExplicitHydrogens);

		tman.setStructure(mol);
		List<IAtomContainer> resultTautomers;

		switch (algorithmType) {
		case Comb_Pure:
			resultTautomers = tman.generateTautomers();
			break;

		case Incremental:
			resultTautomers = tman.generateTautomersIncrementaly();
			break;

		case Comb_Improved:
			resultTautomers = tman.generateTautomers_ImprovedCombApproach();
			break;

		default:
			logger.log(Level.WARNING, "Unsupported algorithm type!");
			return;
		}

		tman.printDebugInfo();

		logger.log(Level.INFO, "\n  Result tautomers: ");
		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol);
		v.add(null);
		for (int i = 0; i < resultTautomers.size(); i++) {
			Double rank = (Double) resultTautomers.get(i).getProperty(TautomerConst.TAUTOMER_RANK);
			if (rank == null)
				rank = new Double(999999);

			Double csRank = (Double) resultTautomers.get(i).getProperty(TautomerConst.CACTVS_ENERGY_RANK);
			if (csRank == null)
				csRank = new Double(999999);

			logger.log(Level.INFO, "   " + rank.toString() + "   CS_RANK = " + csRank.toString() + "   "
					+ moleculeToSMILES(resultTautomers.get(i), false));
			v.add(resultTautomers.get(i));
		}

		logger.log(Level.INFO, "Generated: " + resultTautomers.size() + " tautomers.");

		// preProcessStructures(v);
		TestStrVisualizer tsv = new TestStrVisualizer(v);

	}

	public void visualTestFromFile(String sdfFile) throws Exception {
		logger.log(Level.INFO, "Visual Testing with file " + sdfFile);
		IAtomContainer mol = getMoleculeFromFile(sdfFile);

		String smiles = SmartsHelper.moleculeToSMILES(mol, false);
		logger.log(Level.INFO, "Loaded molecule " + smiles);

		tman.setStructure(mol);

		List<IAtomContainer> resultTautomers = tman.generateTautomersIncrementaly();
		tman.printDebugInfo();

		logger.log(Level.INFO, "\n  Result tautomers: ");
		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol);
		v.add(null);
		for (int i = 0; i < resultTautomers.size(); i++) {
			Double rank = (Double) resultTautomers.get(i).getProperty(TautomerConst.TAUTOMER_RANK);
			if (rank == null)
				rank = new Double(999999);

			logger.log(Level.INFO,
					TautomerStringCode.getCode(resultTautomers.get(i), false, tman.getCodeStrBondSequence()) + "   "
							+ rank.toString() + "   " + SmartsHelper.moleculeToSMILES(resultTautomers.get(i), false));
			v.add(resultTautomers.get(i));
		}

		logger.log(Level.INFO, "Generated: " + resultTautomers.size() + " tautomers.");

		// preProcessStructures(v);
		TestStrVisualizer tsv = new TestStrVisualizer(v);

	}

	void clearAromaticityFlags(IAtomContainer ac) {
		for (int i = 0; i < ac.getAtomCount(); i++) {
			ac.getAtom(i).setFlag(CDKConstants.ISAROMATIC, false);
		}

		for (int i = 0; i < ac.getBondCount(); i++) {
			ac.getBond(i).setFlag(CDKConstants.ISAROMATIC, false);
		}

	}

	void preProcessStructures(List<IAtomContainer> v) throws Exception {
		for (int i = 0; i < v.size(); i++) {
			IAtomContainer ac = v.get(i);
			if (ac == null)
				continue;

			clearAromaticityFlags(ac);

			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
			CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(SilentChemObjectBuilder.getInstance());
			adder.addImplicitHydrogens(ac);
			// AtomContainerManipulator.convertImplicitToExplicitHydrogens(ac);

			CDKHueckelAromaticityDetector.detectAromaticity(ac);

		}
	}

	public void visualTestInChI(String smi) throws Exception {
		logger.log(Level.INFO, "Visual Testing of InChI algorithm: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);

		// Pre-processing (although aromaticity and implicit atoms should be
		// handle from Smiles)
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		CDKHueckelAromaticityDetector.detectAromaticity(mol);

		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(SilentChemObjectBuilder.getInstance());
		adder.addImplicitHydrogens(mol);
		MoleculeTools.convertImplicitToExplicitHydrogens(mol);

		List<IAtomContainer> resultTautomers = itg.getTautomers(mol);

		logger.log(Level.INFO, "\n  Result tautomers: ");
		for (int i = 0; i < resultTautomers.size(); i++)
			logger.log(Level.FINE, "   " + SmartsHelper.moleculeToSMILES(resultTautomers.get(i), false));

		TestStrVisualizer tsv = new TestStrVisualizer(resultTautomers, "InChI");

	}

	public static int testCase(String smi, String expectedTautomers[], boolean FlagPrintTautomers,
			TautomerManager tManager) throws Exception {
		logger.log(Level.INFO, "Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		tManager.setStructure(mol);
		// List<IAtomContainer> resultTautomers = tman.generateTautomers();

		List<IAtomContainer> resultTautomers = tManager.generateTautomersIncrementaly();
		if (FlagPrintTautomers)
			for (int i = 0; i < resultTautomers.size(); i++)
				logger.log(Level.INFO, "   " + SmartsHelper.moleculeToSMILES(resultTautomers.get(i), false));

		int res = checkResultTautomerSet(resultTautomers, expectedTautomers);
		if (res == 0) {
			logger.log(Level.INFO, "Tautomers OK");
			return (0);
		} else {
			logger.log(Level.SEVERE, "Tautomers test error = " + res);
			return (1);
		}

	}

	// helper utilities for the tests
	public static int checkResultTautomerSet(List<IAtomContainer> resultStr, String expectedStr[]) throws Exception {
		// preProcessStructures(resultStr); this is done inside
		// TautomerGenerattor

		SmartsParser sp = new SmartsParser();
		sp.mSupportDoubleBondAromaticityNotSpecified = true;
		IsomorphismTester isoTester = new IsomorphismTester();
		int nNotFound = 0;

		if (resultStr.size() != expectedStr.length)
			return (-1);

		int checked[] = new int[resultStr.size()];
		for (int i = 0; i < checked.length; i++)
			checked[i] = 0;

		for (int i = 0; i < expectedStr.length; i++) {
			IQueryAtomContainer query = sp.parse(expectedStr[i]);
			sp.setNeededDataFlags();
			String errorMsg = sp.getErrorMessages();
			if (!errorMsg.equals("")) {
				logger.log(Level.SEVERE, "Smarts Parser errors:\n" + errorMsg);
				continue;
			}

			isoTester.setQuery(query);

			boolean FlagFound = false;
			for (int k = 0; k < resultStr.size(); k++) {

				// The query must have the same number of atoms and bonds as the
				// result structure
				if (resultStr.get(k).getAtomCount() != query.getAtomCount())
					continue;
				if (resultStr.get(k).getBondCount() != query.getBondCount())
					continue;

				sp.setSMARTSData(resultStr.get(k));
				boolean res = isoTester.hasIsomorphism(resultStr.get(k));
				if (res) {
					FlagFound = true;
					checked[k]++;
					break;
				}
			}

			if (!FlagFound)
				nNotFound++;
		}

		return (nNotFound);
	}

	public int testCaseEnergyRanks(String smi, double expectedRanks[], boolean FlagPrintTautomers, double eps)
			throws Exception {
		logger.log(Level.INFO, "Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		tman.setStructure(mol);
		// List<IAtomContainer> resultTautomers = tman.generateTautomers();

		List<IAtomContainer> resultTautomers = tman.generateTautomersIncrementaly();
		if (FlagPrintTautomers)
			for (int i = 0; i < resultTautomers.size(); i++)
				logger.log(Level.INFO, "   " + SmartsHelper.moleculeToSMILES(resultTautomers.get(i), false));

		int nErrors = 0;

		if (resultTautomers.size() != expectedRanks.length)
			return (9999);

		for (int i = 0; i < resultTautomers.size(); i++) {
			Double d = (Double) resultTautomers.get(i).getProperty(TautomerConst.TAUTOMER_RANK);
			if (d == null)
				throw new Exception("TAUTOMER_RANK is missing");

			boolean FlagFound = false;
			for (int k = 0; k < expectedRanks.length; k++)
				if (Math.abs(d - expectedRanks[k]) <= eps) {
					FlagFound = true;
					break;
				}

			if (!FlagFound)
				nErrors++;
		}

		return nErrors;
	}

	public void testAdenine() throws CDKException, CloneNotSupportedException {
		InChITautomerGenerator tautomerGenerator = new InChITautomerGenerator();
		IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
		IAtomContainer mol = builder.newInstance(IAtomContainer.class);
		IAtom a1 = builder.newInstance(IAtom.class, "N");
		mol.addAtom(a1);
		IAtom a2 = builder.newInstance(IAtom.class, "N");
		mol.addAtom(a2);
		IAtom a3 = builder.newInstance(IAtom.class, "N");
		mol.addAtom(a3);
		IAtom a4 = builder.newInstance(IAtom.class, "N");
		mol.addAtom(a4);
		IAtom a5 = builder.newInstance(IAtom.class, "N");
		mol.addAtom(a5);
		IAtom a6 = builder.newInstance(IAtom.class, "C");
		mol.addAtom(a6);
		IAtom a7 = builder.newInstance(IAtom.class, "C");
		mol.addAtom(a7);
		IAtom a8 = builder.newInstance(IAtom.class, "C");
		mol.addAtom(a8);
		IAtom a9 = builder.newInstance(IAtom.class, "C");
		mol.addAtom(a9);
		IAtom a10 = builder.newInstance(IAtom.class, "C");
		mol.addAtom(a10);
		IAtom a11 = builder.newInstance(IAtom.class, "H");
		mol.addAtom(a11);
		IAtom a12 = builder.newInstance(IAtom.class, "H");
		mol.addAtom(a12);
		IAtom a13 = builder.newInstance(IAtom.class, "H");
		mol.addAtom(a13);
		IAtom a14 = builder.newInstance(IAtom.class, "H");
		mol.addAtom(a14);
		IAtom a15 = builder.newInstance(IAtom.class, "H");
		mol.addAtom(a15);
		IBond b1 = builder.newInstance(IBond.class, a1, a6, IBond.Order.SINGLE);
		mol.addBond(b1);
		IBond b2 = builder.newInstance(IBond.class, a1, a9, IBond.Order.SINGLE);
		mol.addBond(b2);
		IBond b3 = builder.newInstance(IBond.class, a1, a11, IBond.Order.SINGLE);
		mol.addBond(b3);
		IBond b4 = builder.newInstance(IBond.class, a2, a7, IBond.Order.SINGLE);
		mol.addBond(b4);
		IBond b5 = builder.newInstance(IBond.class, a2, a9, IBond.Order.DOUBLE);
		mol.addBond(b5);
		IBond b6 = builder.newInstance(IBond.class, a3, a7, IBond.Order.DOUBLE);
		mol.addBond(b6);
		IBond b7 = builder.newInstance(IBond.class, a3, a10, IBond.Order.SINGLE);
		mol.addBond(b7);
		IBond b8 = builder.newInstance(IBond.class, a4, a8, IBond.Order.SINGLE);
		mol.addBond(b8);
		IBond b9 = builder.newInstance(IBond.class, a4, a10, IBond.Order.DOUBLE);
		mol.addBond(b9);
		IBond b10 = builder.newInstance(IBond.class, a5, a8, IBond.Order.SINGLE);
		mol.addBond(b10);
		IBond b11 = builder.newInstance(IBond.class, a5, a14, IBond.Order.SINGLE);
		mol.addBond(b11);
		IBond b12 = builder.newInstance(IBond.class, a5, a15, IBond.Order.SINGLE);
		mol.addBond(b12);
		IBond b13 = builder.newInstance(IBond.class, a6, a7, IBond.Order.SINGLE);
		mol.addBond(b13);
		IBond b14 = builder.newInstance(IBond.class, a6, a8, IBond.Order.DOUBLE);
		mol.addBond(b14);
		IBond b15 = builder.newInstance(IBond.class, a9, a12, IBond.Order.SINGLE);
		mol.addBond(b15);
		IBond b16 = builder.newInstance(IBond.class, a10, a13, IBond.Order.SINGLE);
		mol.addBond(b16);

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);

		List<IAtomContainer> tautomers = tautomerGenerator.getTautomers(mol);
		// Assert.assertEquals(8, tautomers.size());
		System.out.println("tautomers.size() = " + tautomers.size());

		TestStrVisualizer tsv = new TestStrVisualizer(tautomers, "InChI - algorithm");

	}

	public void testInChIGenerator(String smi) throws Exception {
		logger.log(Level.INFO, "Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi, true);

		InChIGeneratorFactory igf = InChIGeneratorFactory.getInstance();
		InChIGenerator ig = igf.getInChIGenerator(mol);
		String inchi = ig.getInchi();
		String inchiKey = ig.getInchiKey();
		logger.log(Level.INFO, "inchi = " + inchi);
		logger.log(Level.INFO, "inchiKey = " + inchiKey);
	}

	public int testTautomerEquivalence(String smiles) {
		return 0;
	}

	public int testTautomerEquivalence(IAtomContainer ac) throws Exception {
		tman.setStructure(ac);
		List<IAtomContainer> initialTautomers = tman.generateTautomersIncrementaly();
		String expectedTautomers[] = new String[initialTautomers.size()];

		for (int i = 0; i < initialTautomers.size(); i++)
			expectedTautomers[i] = SmartsHelper.moleculeToSMILES(initialTautomers.get(i), false);

		int nErrors = 0;

		for (int i = 0; i < initialTautomers.size(); i++) {
			tman.setStructure(initialTautomers.get(i));
			List<IAtomContainer> resultTautomers = tman.generateTautomersIncrementaly();
			try {
				int res = checkResultTautomerSet(resultTautomers, expectedTautomers);
				if (res != 0)
					nErrors++;
			} catch (Exception e) {
				nErrors++;
			}
		}

		return (nErrors); // OK result = 0
	}

	public void testRuleActivation(String actRules[]) {
		System.out.println("All  Rule List");
		System.out.println("-----------------------------");

		KnowledgeBase kb = tman.getKnowledgeBase();

		ArrayList<String> allRules = kb.getAllRuleNames();
		for (String rname : allRules)
			System.out.println(rname);

		System.out.println();
		System.out.println("Initial Active Rules");
		System.out.println("-----------------------------");
		ArrayList<String> activeRules = kb.getActiveRuleNames();
		for (String rname : activeRules)
			System.out.println(rname);

		if (actRules == null)
			return;

		System.out.println();
		System.out.println("Activating rules:");
		ArrayList<String> ar = new ArrayList<String>();
		for (int i = 0; i < actRules.length; i++) {
			ar.add(actRules[i]);
			System.out.println(actRules[i]);
		}
		kb.setActiveRules(ar);

		System.out.println();
		System.out.println("FinalActive Rules");
		System.out.println("-----------------------------");
		activeRules = kb.getActiveRuleNames();
		for (String rname : activeRules)
			System.out.println(rname);

	}

	public void testConnectStructures(String smi1, int numAt1, String smi2, int numAt2, IBond.Order order)
			throws Exception {
		logger.log(Level.INFO, "Connecting Structures: " + smi1 + "   " + smi2);
		IAtomContainer mol1 = SmartsHelper.getMoleculeFromSmiles(smi1);
		IAtomContainer mol2 = SmartsHelper.getMoleculeFromSmiles(smi2);

		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol1);
		v.add(mol2);
		IAtomContainer newStr = cof.connectStructures(smi1, numAt1, smi2, numAt2, order);
		v.add(newStr);

		logger.log(Level.INFO, "\nResult = " + SmartsHelper.moleculeToSMILES(new AtomContainer(newStr), false));

		TestStrVisualizer tsv = new TestStrVisualizer(v);
	}

	public void testCondenseStructures(String smi1, int str1At0, int str1At1, String smi2, int str2At0, int str2At1)
			throws Exception {
		logger.log(Level.INFO, "Condensing Structures: " + smi1 + "   " + smi2);
		IAtomContainer mol1 = SmartsHelper.getMoleculeFromSmiles(smi1);
		IAtomContainer mol2 = SmartsHelper.getMoleculeFromSmiles(smi2);

		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol1);
		v.add(mol2);
		IAtomContainer newStr = cof.condenseStructures(smi1, str1At0, str1At1, smi2, str2At0, str2At1);
		v.add(newStr);

		logger.log(Level.INFO, "\nResult = " + SmartsHelper.moleculeToSMILES(new AtomContainer(newStr), true));

		TestStrVisualizer tsv = new TestStrVisualizer(v);
	}

	public void testStereoInfo(String smi) throws Exception {
		logger.log(Level.INFO, "Visual Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi, FlagExplicitHydrogens);

		logger.log(Level.INFO, SmartsHelper.getAtomsAttributes(mol));

		String smiles2 = SmartsHelper.moleculeToSMILES(mol, true);
		logger.log(Level.INFO, smiles2);

		List<IAtomContainer> v = new ArrayList<IAtomContainer>();
		v.add(mol);

		// TestStrVisualizer tsv = new TestStrVisualizer(v);

	}

	public void testCACTVSRank(String smi) throws Exception {
		logger.log(Level.INFO, "Testing tautomer CACTVS ranking: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);
		double scoreRank = CACTVSRanking.calcScoreRank(mol);
		logger.log(Level.INFO, "Score = " + scoreRank);
	}

	public void testEnergyRules() throws Exception {
		JsonRuleParser jrp = new JsonRuleParser();
		URL resource = jrp.getClass().getClassLoader().getResource("ambit2/tautomers/energy-rules.json");

		List<EnergyRule> rules = JsonRuleParser.readRuleSetFromJSON(resource.getFile());
		for (int i = 0; i < rules.size(); i++) {
			System.out.println("Rule #" + (i + 1) + "\n" + JsonRuleParser.toJSONString(rules.get(i)) + "\n");
		}

	}

	IAtomContainer getMoleculeFromFile(String fname) throws Exception {
		File file = new File(fname);
		InputStream in = new FileInputStream(file);
		IIteratingChemObjectReader<IAtomContainer> reader = null;

		reader = getReader(in, file.getName());
		IAtomContainer molecule = reader.next();

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
		CDKHueckelAromaticityDetector.detectAromaticity(molecule);

		molecule = AtomContainerManipulator.removeHydrogens(molecule);

		return molecule;
	}

	protected IIteratingChemObjectReader<IAtomContainer> getReader(InputStream in, String extension)
			throws CDKException, AmbitIOException {
		FileInputState instate = new FileInputState();
		return instate.getReader(in, extension);
	}

	public void testStandardizer(String smi) throws Exception {
		logger.log(Level.INFO, "Testing: " + smi);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smi);

		// System.out.println(StereoChemUtils.getStereoElementsStatus(mol));

		// System.out.println(SmartsHelper.getAtomsAttributes(mol));

		IAtomContainer processed = standardprocessor.process(mol);

		// System.out.println(StereoChemUtils.getStereoElementsStatus(processed));
		// System.out.println(SmartsHelper.getAtomsAttributes(processed));

		logger.log(Level.INFO, "   " + SmartsHelper.moleculeToSMILES(processed, false));
	}

	String moleculeToSMILES(IAtomContainer mol, boolean useAromaticityFlag) {
		String smi = null;
		try {
			smi = SmartsHelper.moleculeToSMILES(mol, false);
		} catch (Exception e) {
			if (FlagPrintMoleculeToSmilesExeptions) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return smi;
	}

}
