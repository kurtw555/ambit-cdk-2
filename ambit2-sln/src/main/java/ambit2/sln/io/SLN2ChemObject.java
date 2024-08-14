package ambit2.sln.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.periodictable.PeriodicTable;

import ambit2.sln.SLNAtom;
import ambit2.sln.SLNBond;
import ambit2.sln.SLNConst;
import ambit2.sln.SLNContainer;
import ambit2.smarts.SMIRKSReaction;

/**
 * 
 * @author nick
 * Conversion of SLNContainer to chemical objects represented 
 * on top of CDK (AtomContainer, QueryAtomContainer)
 */

public class SLN2ChemObject 
{
	private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	
	private List<String> conversionErrors = new ArrayList<String>();
	private List<String> conversionWarnings = new ArrayList<String>();
	
	private String currentConversionError = null;
	private String currentConversionWarning = null;

	public List<String> getConversionErrors() {
		return conversionErrors;
	}
	
	public List<String> getConversionWarnings() {
		return conversionWarnings;
	}
	
	public void clearAllErrorsAndWarnings(){
		conversionErrors.clear();
		conversionWarnings.clear();
	}
	
	public String getAllErrors()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < conversionErrors.size(); i++)
			sb.append(conversionErrors.get(i) + "\n");
		return sb.toString();
	}
	
	public LinearNotationType getCompatibleNotation(SLNContainer slnContainer)	{
		//TODO
		return null;
	}
	
	public SLNContainer atomContainerToSLNContainer(IAtomContainer container)
	{
		SLNContainer slnContainer = new SLNContainer(null);

		Map<IAtom, SLNAtom> convertedAtoms = new HashMap<IAtom, SLNAtom>();
		for (int i = 0; i < container.getAtomCount(); i++)
		{	
			IAtom atom = container.getAtom(i);
			SLNAtom slnAtom = atomToSLNAtom(atom);
			if (currentConversionWarning != null)
				conversionWarnings.add(currentConversionWarning + " for atom: " + (i+1));
			if (slnAtom == null)
			{
				conversionErrors.add(currentConversionError + " for atom: " + (i+1));
				continue;
			}
			slnContainer.addAtom(slnAtom);
			convertedAtoms.put(atom, slnAtom);
		}

		for (int i = 0; i < container.getBondCount(); i++)
		{
			IBond bond = container.getBond(i);
			SLNBond slnBond = bondToSLNBond(bond);
			if (currentConversionWarning != null)
				conversionWarnings.add(currentConversionWarning + " for bond: " + (i+1));
			if (slnBond == null)
			{
				conversionErrors.add(currentConversionError + " for bond: " + (i+1));
				continue;
			}
			SLNAtom newAtoms[] = new SLNAtom[2];
			newAtoms[0] = convertedAtoms.get(bond.getAtom(0));
			newAtoms[1] = convertedAtoms.get(bond.getAtom(1));
			if (newAtoms[0] == null || newAtoms[1] == null)
				continue; //one of the atoms is not converted
			slnBond.setAtoms(newAtoms);
			slnContainer.addBond(slnBond);
		}

		return slnContainer;
	}
	
	public IAtomContainer  slnContainerToAtomContainer(SLNContainer slnContainer)
	{
		 IAtomContainer container = new AtomContainer();
		 
		 Map<SLNAtom, IAtom> convertedAtoms = new HashMap<SLNAtom, IAtom>();

		 for (int i = 0; i < slnContainer.getAtomCount(); i++)
		 {
			 SLNAtom slnAtom = (SLNAtom) slnContainer.getAtom(i);
			 IAtom atom = slnAtomToAtom(slnAtom);
			 if (currentConversionWarning != null)
				 conversionWarnings.add(currentConversionWarning + " for atom: " + (i+1));
			 if (atom == null)
			 {
				 conversionErrors.add(currentConversionError + " for atom: " + (i+1));
				 continue;
			 }
			 container.addAtom(atom);
			 convertedAtoms.put(slnAtom, atom);
		 }

		 for (int i = 0; i < slnContainer.getBondCount(); i++)
		 {
			 SLNBond slnBbond = (SLNBond) slnContainer.getBond(i);
			 IBond bond = slnBondToBond(slnBbond);
			 if (currentConversionWarning != null)
				 conversionWarnings.add(currentConversionWarning + " for bond: " + (i+1));
			 if (bond == null)
			 {
				 conversionErrors.add(currentConversionError + " for bond: " + (i+1));
				 continue;
			 }
			 IAtom newAtoms[] = new IAtom[2];
			 newAtoms[0] = convertedAtoms.get(slnBbond.getAtom(0));
			 newAtoms[1] = convertedAtoms.get(slnBbond.getAtom(1));
			 if (newAtoms[0] == null || newAtoms[1] == null)
				continue; //one of the atoms is not converted
			 bond.setAtoms(newAtoms);
			 container.addBond(bond);
		 }

		 return container;
	}
	
	public IQueryAtomContainer slnContainerToQueryAtomContainer(SLNContainer container)
	{
		//TODO
		return null;
	}
	
	public  SLNContainer QueryAtomContainerToSLNContainer(IQueryAtomContainer query)
	{
		//TODO
		return null;
	}
	
	public SMIRKSReaction slnContainerToSMIRKSReaction(SLNContainer container)
	{
		//TODO
		return null;
	}
	
	public  SLNContainer SMIRKSReactionToSLNContainer(SMIRKSReaction reaction)
	{
		//TODO
		return null;
	}
	
	public SLNAtom atomToSLNAtom(IAtom atom)
	{
		currentConversionError = null;
		currentConversionWarning = null;
		if (atom == null)
		{	
			currentConversionError = "Atom is null";
			return null;
		}	
		SLNAtom slnAt = new SLNAtom(builder);
		String atomName = atom.getSymbol();
		for (int i = 0; i < SLNConst.elSymbols.length; i++)
			if (atomName.equals(SLNConst.elSymbols[i]))
			{	
				slnAt.atomType = i;
				break;
			}	
		slnAt.atomName = atomName;
		//TODO handle H atoms
		
		return slnAt;
	}
	
	/*
	 * Converts only the bond type/expression info
	 * connected atoms info is not handled 
	 */
	public SLNBond bondToSLNBond(IBond bond)
	{
		currentConversionError = null;
		currentConversionWarning = null;
		if (bond == null)
		{	
			currentConversionError = "Bond is null";
			return null;
		}	
		SLNBond slnBo = new SLNBond(builder);
		slnBo.bondType = bond.getOrder().ordinal() + 1;
		return slnBo;
	}
	
	public IAtom slnAtomToAtom(SLNAtom slnAt)
	{
		currentConversionError = null;
		currentConversionWarning = null;
		if (slnAt == null)
        {
            currentConversionError = "SNLAtom is null";
            return null;
        }
        
        if ((slnAt.atomType > 0)  && (slnAt.atomType < SLNConst.GlobDictOffseet))
        {
        	if (slnAt.atomType < SLNConst.elSymbols.length)
        	{	
        		IAtom atom = new Atom();
        		atom.setSymbol(SLNConst.elSymbols[slnAt.atomType]);
        		atom.setAtomicNumber(PeriodicTable.getAtomicNumber(SLNConst.elSymbols[slnAt.atomType]));
        		atom.setImplicitHydrogenCount(0);
        		//TODO handle H atoms, charge, isotope
        		return atom;
        	}
        	else
        	{
        		currentConversionError = "SNLAtom type is incorrect: " + slnAt.atomType;
            	return null;
        	}
        }
        else
        {
        	currentConversionError = "SNLAtom type is not defined";
        	return null;
        }  
	}
	
	/*
	 * Converts only the bond type/expression info
	 * connected atoms info is not handled 
	 */
	public IBond slnBondToBond(SLNBond slnBo)
	{
		currentConversionError = null;
		currentConversionWarning = null;
		if (slnBo == null)
        {
            currentConversionError = "Bond is null";
            return null;
        }
		
		if (slnBo.bondType != 0)
		{
			IBond bond = new Bond();
			switch (slnBo.bondType)
			{
			case 1:
				bond.setOrder(IBond.Order.SINGLE);
				break;
			case 2:
				bond.setOrder(IBond.Order.DOUBLE);
				break;
			case 3:
				bond.setOrder(IBond.Order.TRIPLE);
				break;	
			}
			return bond;
		}
		else
		{		
			currentConversionError = "Bond type is not defined";
			return null;
		}	
	}
	
	public SLNAtom queryAtomToSLNAtom(IQueryAtom queryAtom)
    {
        currentConversionError = null;
        currentConversionWarning = null;
        //TODO
        return null;
    }
	
	/*
	 * Converts only the bond type/expression info
	 * connected atoms info is not handled 
	 */
    public SLNBond queryBondToSLNBond(IQueryBond queryBond)
    {
        currentConversionError = null;
        currentConversionWarning = null;
        //TODO
        return null;
    }

    public IQueryAtom slnAtomToQueryAtom(SLNAtom slnAt)
    {
        currentConversionError = null;
        currentConversionWarning = null;
        //TODO
        return null;
    }

    /*
	 * Converts only the bond type/expression info
	 * connected atoms info is not handled 
	 */
    public IQueryBond slnBondToQueryBond(SLNBond slnBo)
    {
        currentConversionError = null;
        currentConversionWarning = null;
        //TODO
        return null;
    }

	
}
