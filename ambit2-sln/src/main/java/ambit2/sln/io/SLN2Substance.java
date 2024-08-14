package ambit2.sln.io;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import ambit2.base.data.StructureRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.composition.CompositionRelation;
import ambit2.base.relation.composition.Proportion;
import ambit2.sln.SLNContainer;
import ambit2.sln.SLNContainerSet;
import ambit2.smarts.SmartsHelper;

public class SLN2Substance 
{
	//Conversion flags 
	public boolean FlagProportion = true;
	public boolean FlagCompositionUUID = true;
	public boolean FlagSmiles = true;
	public boolean FlagInchi = true;
	public boolean FlagInchiKey = true;
	public boolean FlagRelation = true;
	public boolean FlagContent = true;
	public boolean FlagProperties = true;
	public boolean FlagStrType = true;
	public boolean FlagFacets = true;
	public boolean FlagRelationMetric = true;
	public boolean FlagRelationType = true;
	/*
	public boolean FlagReference = false;
	public boolean FlagSelected = false;
	public boolean FlagDataEntryID = false;
	public boolean FlagId_srcdataset = false;
	public boolean FlagIdchemical = false;
	public boolean FlagIdstructure = false;
	*/
	
	
	//Conversion attribute names for CompositionRelation fields
	public String proportion_SLNAttr = "proportion";
	public String compositionUUID_SLNAttr = "compositionUUID";
	public String name_SLNAttr = "name";
	public String relationMetric_SLNAttr = "proportion";  //the field name in class CompositionRelation is "relation"
	public String relationType_SLNAttr = "role";
	
	//Conversion attribute names for Structure Record fields
	public String inchiKey_SLNAttr = "inchiKey";
	public String formula_SLNAttr = "formula";
	public String idchemical_SLNAttr = "idchemical";
	public String idstructure_SNLAttr = "idstricture";
	public String content_SNLAttr = "content";
	public String format_SLNAttr = "format";
	public String reference_SLNAttr = "reference";
	public String properties_SLNAttr = "properties";
	public String type_SLNAttr = "type";
	/*
	public String selected_SLNAttr = "selected";
	public String facets_SLNAttr = "facets";
	public String dataEntryID_SLNAttr = "dataEntryID";
	public String id_srcdataset_SLNAttr = "id_srcdataset";
	*/
	
	public boolean FlagAddImplicitHAtomsOnSLNAtomConversion = false;
	
	private List<String> conversionErrors = new ArrayList<String>();
	private List<String> conversionWarnings = new ArrayList<String>();
	public SLN2ChemObject sln2ChemObject = new SLN2ChemObject();
	
	
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
	
	
	public List<CompositionRelation> slnToSubstanceComposition(SLNContainerSet slnContSet)
	{
		if (slnContSet == null)
			return null;
		
		List<CompositionRelation> composition = new ArrayList<CompositionRelation>();
		for (int i = 0; i < slnContSet.containers.size(); i++)
		{
			SLNContainer slnContainer = slnContSet.containers.get(i);
			CompositionRelation compRel = slnContainerToCompositionRelation(slnContainer);
			composition.add(compRel);
		}
		
		return composition;
	}
	
	public SLNContainerSet substanceCompositionToSln(List<CompositionRelation> composition)
	{
		SLNContainerSet slnContSet = new SLNContainerSet();
		for (int i = 0; i < composition.size(); i++)
		{
			CompositionRelation compRel = composition.get(i);
			SLNContainer slnContainer = compositionRelationToSLNContainer(compRel);
			slnContSet.containers.add(slnContainer);
		}
		return slnContSet;
	}
	
	
	public CompositionRelation slnContainerToCompositionRelation(SLNContainer slnContainer)
	{
		IStructureRecord structure = slnContainerToStructureRecord(slnContainer);
		CompositionRelation compRel = new CompositionRelation(null, structure, null, null);
		
		if (FlagCompositionUUID)
		{
			String attr = slnContainer.getAttributes().userDefiendAttr.get(compositionUUID_SLNAttr);
			if (attr != null)
				compRel.setCompositionUUID(attr);
		}
		
		if (FlagRelationMetric)
		{
			String attr = slnContainer.getAttributes().userDefiendAttr.get(relationMetric_SLNAttr);
			if (attr != null)
			{
				try
				{
					Proportion prop = SLNIOHelpers.proportionFromString(attr);
					compRel.setRelation(prop);
				}
				catch (Exception e)	
				{
					//Handle error
				}
			}
		}
		
		//TODO
		return compRel;
	}
	
	public SLNContainer compositionRelationToSLNContainer(CompositionRelation compRel)
	{
		SLNContainer container;
		if (compRel.getSecondStructure() != null)
			container = structureRecordToSLNContainer(compRel.getSecondStructure());
		else
			container = new SLNContainer(SilentChemObjectBuilder.getInstance());
		
		if (FlagCompositionUUID)
		{
			String attr = compRel.getCompositionUUID();
			if (attr != null)
				container.getAttributes().userDefiendAttr.put(compositionUUID_SLNAttr, attr);
		}
		
		if (FlagRelationMetric)
		{
			Proportion prop = compRel.getRelation();
			if (prop != null)
			{	
				try
				{
					String attr = SLNIOHelpers.proportionToString(prop);
					if (attr != null)
						container.getAttributes().userDefiendAttr.put(relationMetric_SLNAttr, attr);
				}
				catch (Exception e)	
				{
					//Handle error
				}
			}
			
		}
		
		return container;
	}
	
	public IStructureRecord slnContainerToStructureRecord(SLNContainer slnContainer)
	{
		if (slnContainer == null)
			return null;
		
		IAtomContainer container = sln2ChemObject.slnContainerToAtomContainer(slnContainer);
		try{
			SmartsHelper.preProcessStructure(container, true, FlagAddImplicitHAtomsOnSLNAtomConversion);
		}
		catch(Exception e)
		{
			conversionErrors.add("SSMILES generation error: " + e.getMessage());
		}
		conversionErrors.addAll(sln2ChemObject.getConversionErrors());
		conversionWarnings.addAll(sln2ChemObject.getConversionWarnings());
		
		IStructureRecord structure = new StructureRecord();
		if (FlagSmiles)
		{
			try{
				String smiles = SmartsHelper.moleculeToSMILES(container, true);
				structure.setSmiles(smiles);
			}
			catch(Exception e)
			{
				conversionErrors.add("SMILES generation error: " + e.getMessage());
			}
		}
		
		return structure;
	}
	
	public SLNContainer structureRecordToSLNContainer(IStructureRecord structure)
	{
		if (structure == null)
			return null;
		
		SLNContainer slnContainer = null; 
		
		if (structure.getSmiles() != null)
		{
			IAtomContainer container = null;
			try{
				container = SmartsHelper.getMoleculeFromSmiles(structure.getSmiles());
			}
			catch(Exception e)
			{
				conversionErrors.add("Incorrect SMILES: " + e.getMessage());
			}
			
			if (container != null)
			{	
				slnContainer = sln2ChemObject.atomContainerToSLNContainer(container);
				conversionErrors.addAll(sln2ChemObject.getConversionErrors());
				conversionWarnings.addAll(sln2ChemObject.getConversionWarnings());
			}
		}
		
		//TODO
		
		
		if (slnContainer == null)
			slnContainer = new SLNContainer(SilentChemObjectBuilder.getInstance());
		
		return slnContainer;
	}
	
	
	
}
