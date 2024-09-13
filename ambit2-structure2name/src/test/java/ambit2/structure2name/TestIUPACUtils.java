package ambit2.structure2name;

import org.openscience.cdk.interfaces.IAtomContainer;

import ambit2.smarts.SmartsHelper;
import ambit2.structure2name.rules.IUPACRuleDataBase;

public class TestIUPACUtils 
{
	public static void main(String[] args) throws Exception 
	{
		testIUPACRuleDB();
		//testIUPACNameGen("CCCCC1CCC1");
	}
	
	public static void testIUPACRuleDB() throws Exception
	{
		IUPACRuleDataBase rdb = IUPACRuleDataBase.getDefaultRuleDataBase();
		System.out.println("Carbon data:");
		for (int i = 0; i < rdb.carbonData.length; i++)
			System.out.println(rdb.carbonData[i].toString());
		System.out.println("Functional groups data:");
		for (int i = 0; i < rdb.functionalGroups.length; i++)
			System.out.println(rdb.functionalGroups[i].toString());
		
		
	}
	
	public static void testIUPACNameGen(String smiles) throws Exception
	{
		System.out.println("Testing IUPACNameGenerator for " + smiles);
		IAtomContainer mol = SmartsHelper.getMoleculeFromSmiles(smiles);
		IUPACNameGenerator gen = new IUPACNameGenerator();
		
		String name = gen.generateIUPACName(mol);
		String s = gen.getComponentDataAsString();
		System.out.println(s);
		
	}
}
