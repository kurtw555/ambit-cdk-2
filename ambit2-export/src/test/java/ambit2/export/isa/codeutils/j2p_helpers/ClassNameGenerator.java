package ambit2.export.isa.codeutils.j2p_helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ambit2.export.isa.codeutils.Json2Pojo;

public class ClassNameGenerator 
{
	private Json2Pojo j2p = null;
	
	//Preliminary list of class names which are to be added for
	//variables of type object
	public Map<String, String> classNameSuggestions = new HashMap<String, String>();
	
	public boolean FlagRemoveSuffix = true;
	public String suffix = "_schema";
	
	public boolean FlagHandleTokensInSchemaName = true;
	public boolean FlagHandleTokensInVarName = false;
	public String splitters[] = new String[] {"_", "-", " ", "."}; 
	
	public String additionSuffixForDuplication = "_";
	
	
	//work variables
	List<TokenPars> tokens = new ArrayList<TokenPars>();
	List<TokenPars> allTokens = new ArrayList<TokenPars>();
	
	public ClassNameGenerator(Json2Pojo j2p)
	{
		this.j2p = j2p;
	}
	
	public String getJavaClassNameForSchema(String schemaName)
	{
		String className = schemaName;
		if (FlagRemoveSuffix)
		{
			if (schemaName.endsWith(suffix))
				className = schemaName.substring(0, schemaName.length() - suffix.length());
		}
		
		if (FlagHandleTokensInSchemaName)
		{
			String tok[] = tokenize(className);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < tok.length; i++)
			{
				sb.append(capitalyzeFirstChar(tok[i]));
			}
			className = sb.toString();
		}
		
		return j2p.checkForDuplication(className);
		
	}
	
	
	
	public String getJavaClassNameForVariable(String varName, boolean flagCleanPlural)
	{
		String className = null;
		
		if (FlagHandleTokensInVarName)
		{
			String tok[] = tokenize(varName);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < tok.length; i++)
			{
				sb.append(capitalyzeFirstChar(tok[i]));
			}
			className =  sb.toString();
		}
		else
		{
			className = capitalyzeFirstChar(varName);
		}
		
		if (flagCleanPlural)
		{
			if (className.endsWith("s"))
				className = className.substring(0, className.length()-1);
		}
		
		return j2p.checkForDuplication(className);
	}
	
	public String getJavaEnumNameForEnumVariable(String varName)
	{
		String enumName = null;
		
		if (FlagHandleTokensInVarName)
		{
			String tok[] = tokenize(varName);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < tok.length; i++)
			{
				sb.append(capitalyzeFirstChar(tok[i]));
			}
			enumName =  sb.toString();
		}
		else
		{
			enumName = capitalyzeFirstChar(varName);
		}
		
		
		return enumName;
	}
	
	public String getJavaEnumElementValue(String element)
	{
		String enumValue = null;


		String tok[] = tokenize(element);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tok.length; i++)
		{
			sb.append(tok[i].toUpperCase());
			if (i < tok.length - 1)
				sb.append("_");
		}
		enumValue =  sb.toString();
		return enumValue;
	}
	
	
	public String[] tokenize(String s)
	{
		tokens.clear();
		allTokens.clear();
		
		int pos = 0;
		int copyPos, nextMinPos, i;
		int currentBeginSplitter = -1;
		int ind, splitType = -1;
		int numSplitters = splitters.length;
		
		//Check for splitters in the beginning of the string
		boolean startSplitters = false;
		for (i=0; i < numSplitters; i++)
		{
			if (s.startsWith(splitters[i])){
				pos = pos + splitters[i].length();
				startSplitters = true;
				currentBeginSplitter = i;
				break;
			}
		}
		
		while (startSplitters)
		{
			startSplitters = false;
			for (i=0; i < numSplitters; i++){
				if (s.startsWith(splitters[i],pos)){
					allTokens.add(new TokenPars(pos,pos,currentBeginSplitter,i));
					pos = pos + splitters[i].length();
					currentBeginSplitter = i;
					startSplitters = true;
					break;
				}
			}
		}
		
		copyPos = pos;
		
		//Detecting all tokens
		while (pos < s.length()) 
		{
			//Finding the next splitter
			nextMinPos = s.length();
			splitType  = -1;
			for (i=0; i < numSplitters; i++)
			{
				ind = s.indexOf(splitters[i],pos);
				if ((ind != -1)&(ind < nextMinPos))
				{
					nextMinPos = ind;
					splitType = i;
				}
			}
			
			if (splitType == -1)
			{
				//This is the last Token reaching the string End
				tokens.add(new TokenPars(copyPos,s.length(),currentBeginSplitter,splitType));
				allTokens.add(new TokenPars(copyPos,s.length(),currentBeginSplitter,splitType));
				pos = s.length();
				break;
			};
			
			boolean FlagAdd = true;
			
			if (nextMinPos == copyPos) 
			{	
				//Empty Token is added
				if (FlagAdd)
					allTokens.add(new TokenPars(copyPos,copyPos,currentBeginSplitter,splitType));
				pos = nextMinPos + splitters[splitType].length();
				if (FlagAdd)
					copyPos = pos;
				currentBeginSplitter = splitType;
			}
			else
			{
				//System.out.println(pos+"    "+nextMinPos);
				if (FlagAdd)
				{	
					tokens.add(new TokenPars(copyPos,nextMinPos,currentBeginSplitter,splitType));
					allTokens.add(new TokenPars(copyPos,nextMinPos,currentBeginSplitter,splitType));
				}
				pos = nextMinPos + splitters[splitType].length();
				if (FlagAdd)
					copyPos = pos;
				currentBeginSplitter = splitType;
			}
		}// of while pos < s.length()
		
		String result[] = new String[tokens.size()];
		for (i = 0; i< tokens.size(); i++)
			result[i] = s.substring(tokens.get(i).iBegin, tokens.get(i).iEnd);
		
		return result;
	}//End of tokenize
	
	String capitalyzeFirstChar(String s)
	{
		if (s.length() > 1)
		{	
			String s1 = s.substring(0, 1);
			String s2 = s.substring(1);
			return s1.toUpperCase()+s2;
		}
		else
			return s.toUpperCase();
	}
	
	String capitalyzeFirstCharAndLowerCaseOtherChars(String s)
	{
		if (s.length() > 1)
		{	
			String s1 = s.substring(0, 1);
			String s2 = s.substring(1);
			return s1.toUpperCase()+s2.toLowerCase();
		}
		else
			return s.toUpperCase();
	}
	
}

class TokenPars{
	int iBegin; //Token indexes which define the corresponding substring
	int iEnd;
	int splitterBegin, splitterEnd; //Splitter types
	int indexAll; //This is the token corresponding index in the "All" Vector
	
	public TokenPars(int iBegin_, 	int iEnd_, 	int spliterBegin_, int spliterEnd_){
		iBegin = iBegin_;
		iEnd = iEnd_;
		splitterBegin = spliterBegin_;
		splitterEnd = spliterEnd_;
	}
}// End of class TokenPars
