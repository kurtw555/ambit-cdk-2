package ambit2.core.external.test;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import ambit2.base.external.CommandShell;
import ambit2.core.external.ShellSDFoutput;
import ambit2.core.smiles.OpenBabelShell;
import junit.framework.Assert;

public class CommandShellTest {
	protected CommandShell shell;
	@Before
	public void setUp() throws Exception {
		shell = new ShellSDFoutput() {
			@Override
			protected Object transform(Object mol) {
				return mol;
			}
		};

		File homeDir = new File(System.getProperty("java.io.tmpdir") +"/.ambit2/" + System.getProperty("user.name") + "/obabel/*");
		homeDir.delete();
		

	}

	@Test	
	public void testOpenBabel() throws Exception {
		String testfile = "babel_test.sdf";
		OpenBabelShell babel = new OpenBabelShell();
		babel.setOutputFile(testfile);
		IAtomContainer newmol = babel.runShell("c1ccccc1");

		IAtomContainer c = AtomContainerManipulator.removeHydrogensPreserveMultiplyBonded(newmol);
		
		IAtomContainer mol = MoleculeFactory.makeBenzene();
		UniversalIsomorphismTester uit = new UniversalIsomorphismTester();
		Assert.assertTrue(uit.isIsomorph(mol,c));
		new File(babel.getOutputFile()).delete();
	}	


}

