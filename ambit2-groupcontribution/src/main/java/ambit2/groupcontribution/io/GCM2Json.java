package ambit2.groupcontribution.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ambit2.groupcontribution.GroupContributionModel;
import ambit2.groupcontribution.GroupContributionModel.Type;
import ambit2.groupcontribution.correctionfactors.ICorrectionFactor;
import ambit2.groupcontribution.correctionfactors.SmartsCorrectionFactor;
import ambit2.groupcontribution.groups.AtomGroup;
import ambit2.groupcontribution.groups.BondGroup;
import ambit2.groupcontribution.groups.DGroup;
import ambit2.groupcontribution.groups.GGroup;
import ambit2.groupcontribution.groups.IGroup;
import ambit2.rules.json.JSONParsingUtils;
import ambit2.smarts.IsomorphismTester;
import ambit2.smarts.SmartsParser;


public class GCM2Json 
{
	private final static Logger logger = Logger.getLogger(GCM2Json.class.getName());
	
	public ArrayList<String> configErrors = new ArrayList<String>();
	public ArrayList<String> configWarnings = new ArrayList<String>();
	
	private IsomorphismTester isoTester = null;
	private SmartsParser parser = null;
	
		
	public GCM2Json() {
		isoTester = new IsomorphismTester();
		parser = new SmartsParser();
	}
	
	public GCM2Json(IsomorphismTester isoTester, SmartsParser parser) {
		this.isoTester = isoTester;
		this.parser = parser;
	}
	
	public GroupContributionModel loadFromJSON(File jsonConfig) throws Exception 
	{
		FileInputStream fin = new FileInputStream(jsonConfig);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;

		try {
			root = mapper.readTree(fin);
		} catch (Exception x) {
			throw x;
		} finally {
			try {
				fin.close();
			} catch (Exception x) {
			}
		}

		//JSONParsingUtils jsonUtils = new JSONParsingUtils();
		GroupContributionModel gcm = new GroupContributionModel();
		GroupContributionModel.GCMConfigInfo addConfigInfo = gcm.getAdditionalConfig();
		
		JsonNode curNode = root.path("MODEL_NAME");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())
				gcm.setModelName(curNode.asText());
			else
				configErrors.add("MODEL_NAME is not textual!");
		}
		
		curNode = root.path("MODEL_DESCRIPTION");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())
				gcm.setModelDescription(curNode.asText());
			else
				configErrors.add("MODEL_DESCRIPTION is not textual!");
		}
		
		curNode = root.path("TARGET_PROPERTY");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())
				gcm.setTargetProperty(curNode.asText());
			else
				configErrors.add("TARGET_PROPERTY is not textual!");
		}
		
		curNode = root.path("MODEL_TYPE");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())
				addConfigInfo.gcmTypeString = curNode.asText();				
			else
				configErrors.add("MODEL_TYPE is not textual!");
		}
		
		curNode = root.path("LOCAL_DESCRIPTORS");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.localDescriptorsString = curNode.asText();
			else
				configErrors.add("LOCAL_DESCRIPTORS is not textual!");
		}
		
		curNode = root.path("GLOBAL_DESCRIPTORS");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.globalDescriptorsString = curNode.asText();
			else
				configErrors.add("GLOBAL_DESCRIPTORS is not textual!");
		}
		
		curNode = root.path("CORRECTION_FACTORS");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.corFactorsString = curNode.asText();
			else
				configErrors.add("CORRECTION_FACTORS is not textual!");
			//TODO handle non textual json node
		}
		
		try {
			Double d = JSONParsingUtils.extractDoubleKeyword(root, "COLUMN_FILTRATION_THRESHOLD", false);
			if (d != null)
				addConfigInfo.columnFiltrationthreshold = d;	
		}
		catch (Exception e){
			configErrors.add(e.getMessage());		
		}
		
		try {
			Integer i = JSONParsingUtils.extractIntKeyword(root, "FRACTION_DIGITS", false);
			if (i != null)
				addConfigInfo.fractionDigits = i;	
		}
		catch (Exception e){
			configErrors.add(e.getMessage());		
		}
		
		curNode = root.path("VALIDATION");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.validationString = curNode.asText();
			else
				configErrors.add("VALIDATION is not textual!");			
		}
		
		curNode = root.path("TRAINING_SET_FILE");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.trainingSetFile = curNode.asText();
			else
				configErrors.add("TRAINING_SET_FILE is not textual!");			
		}
		
		curNode = root.path("EXTERNAL_SET_FILE");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isTextual())			
				addConfigInfo.externalSetFile = curNode.asText();
			else
				configErrors.add("EXTERNAL_SET_FILE is not textual!");			
		}
		
		curNode = root.path("CALCULATED_GROUPS");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isArray())			
			{
				for (int i = 0; i < curNode.size(); i++)
				{					
					IGroup g = getGroupFromJsonNode (curNode.get(i), i);
					if (g != null)
						gcm.addGroup(g);
				}
			}
			else
				configErrors.add("CALCULATED_GROUPS is not array!");			
		}
		
		curNode = root.path("CALCULATED_CORRECTION_FACTORS");
		if (!curNode.isMissingNode())
		{	
			if (curNode.isArray())			
			{
				for (int i = 0; i < curNode.size(); i++)
				{	
					ICorrectionFactor cf = getCorrectionFactorFromJsonNode(curNode.get(i), i);
					if (cf != null)
						gcm.addCorrectionFactor(cf);
				}
			}
			else
				configErrors.add("CALCULATED_CORRECTION_FACTORS is not array!");			
		}
				
		return gcm;
	}
	
	IGroup getGroupFromJsonNode (JsonNode node, int arrayIndex)
	{
		IGroup group = null;
		IGroup.Type type = null;
		String designation = null;
		Double contribution = null;
		
		JsonNode jsnod = node.path("TYPE");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_GROUPS, element #" + (arrayIndex+1) + ", TYPE is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isTextual())
			{
				configErrors.add("In section CALCULATED_GROUPS element #" + (arrayIndex+1) + " TYPE is not text!");
				return null;
			}
			type = IGroup.Type.fromString(jsnod.asText());
			if (type == null)
			{
				configErrors.add("In section CALCULATED_GROUPS element #" + (arrayIndex+1) + " TYPE is not correct!");
				return null;
			}
		}
		
		jsnod = node.path("DESIGNATION");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_GROUPS, element #" + (arrayIndex+1) + " DESIGNATION is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isTextual())
			{
				configErrors.add("In section CALCULATED_GROUPS element #" + (arrayIndex+1) + " DESIGNATION is not text!");
				return null;
			}
			designation = jsnod.asText();
		}
		
		jsnod = node.path("CONTRIBUTION");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_GROUPS, element #" + (arrayIndex+1) + " CONTRIBUTION is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isDouble())
			{
				configErrors.add("In section CALCULATED_GROUPS element #" + (arrayIndex+1) + " CONTRIBUTION is not double!");
				return null;
			}
			contribution = jsnod.asDouble();
		}
		
		group = generateGroup(type, designation);
		group.setContribution(contribution);
		return group;
	}
	
	IGroup generateGroup(IGroup.Type type, String designation)
	{	
		switch (type)
		{
		case ATOM:
			AtomGroup ag = new AtomGroup();
			ag.setAtomDesignation(designation);
			return ag;
		case BOND:
			BondGroup bg = new BondGroup();
			bg.setBondDesignation(designation);
			return bg;
		case G_GROUP:
			GGroup gg = new GGroup();
			gg.setGroupDesignation(designation);
			return gg;
		case D_GROUP:
			DGroup dg = new DGroup();
			dg.setGroupDesignation(designation);
			return dg;			
		}
		return null;
	}
	
		
	ICorrectionFactor getCorrectionFactorFromJsonNode(JsonNode node, int arrayIndex)
	{
		ICorrectionFactor cf = null;
		ICorrectionFactor.Type type = null;
		String designation = null;
		Double contribution = null;
		Object params[] = null;
		
		JsonNode jsnod = node.path("TYPE");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_CORRECTION_FACTORS, element #" 
					+ (arrayIndex+1) + ", TYPE is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isTextual())
			{
				configErrors.add("In section CALCULATED_CORRECTION_FACTORS element #" 
						+ (arrayIndex+1) + " TYPE is not text!");
				return null;
			}
			type = ICorrectionFactor.Type.fromString(jsnod.asText());
			if (type == null)
			{
				configErrors.add("In section CALCULATED_CORRECTION_FACTORS element #" 
						+ (arrayIndex+1) + " TYPE is not correct!");
				return null;
			}
		}
		
		jsnod = node.path("DESIGNATION");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_CORRECTION_FACTORS, element #" 
					+ (arrayIndex+1) + " DESIGNATION is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isTextual())
			{
				configErrors.add("In section CALCULATED_CORRECTION_FACTORS element #" 
					+ (arrayIndex+1) + " DESIGNATION is not text!");
				return null;
			}
			designation = jsnod.asText();
		}
		
		jsnod = node.path("CONTRIBUTION");
		if (jsnod.isMissingNode())
		{
			configErrors.add("In section CALCULATED_CORRECTION_FACTORS, element #" 
					+ (arrayIndex+1) + " CONTRIBUTION is missing!");
			return null;
		}
		else
		{
			if (!jsnod.isDouble())
			{
				configErrors.add("In section CALCULATED_CORRECTION_FACTORS element #" 
						+ (arrayIndex+1) + " CONTRIBUTION is not double!");
				return null;
			}
			contribution = jsnod.asDouble();
		}
		
		try {
			params = JSONParsingUtils.extractArrayKeyword(node, "PARAMETERS", true);
		}
		catch (Exception x){
			configErrors.add("In section CALCULATED_CORRECTION_FACTORS, element #" 
					+ (arrayIndex+1) + " " + x.getMessage() );
			return null;
		}
		
		try
		{
			cf = generateCorrectionFactor(type, designation, params);
			cf.setContribution(contribution);
			return cf;
		}	
		catch (Exception x) {
			configErrors.add("In section CALCULATED_CORRECTION_FACTORS, element #" 
					+ (arrayIndex+1) + " " + x.getMessage());
		}
		
		return null;
	}
	
	ICorrectionFactor generateCorrectionFactor(ICorrectionFactor.Type type, 
				String designation, Object params[]) throws Exception
	{	
		switch (type)
		{
		case SMARTS:
			//Check params
			if (params == null)
				throw new Exception("no parameters for SMARTS correction factor");
			if (params.length > 0)
			{
				if (!(params[0] instanceof String))
					throw new Exception("First parameter for SMARTS correction factor is not string");
			}
			//Create corr. factor
			SmartsCorrectionFactor cf = new SmartsCorrectionFactor((String)params[0], parser, isoTester);
			if (cf.getError().equals(""))
				return cf;
			else
				throw new Exception("Errors on creating SMARTS correction factor: " + cf.getError());
			
		case ATOM_PAIR:
			//TODO
			break;
		}
		
		return null;
	}
	
	public String getAllErrorsAsString()
	{
		StringBuffer sb = new StringBuffer();
		for (String err : configErrors)
			sb.append(err + "\n");
		return sb.toString();
	}
	
	public String getAllWarningAsString()
	{
		StringBuffer sb = new StringBuffer();
		for (String w : configWarnings)
			sb.append(w + "\n");
		return sb.toString();
	}
	
}
