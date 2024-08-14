package ambit2.groupcontribution;

import java.util.ArrayList;
import java.util.List;

import ambit2.groupcontribution.correctionfactors.DescriptorInfo;
import ambit2.groupcontribution.descriptors.ILocalDescriptor;
import ambit2.groupcontribution.descriptors.LocalDescriptorManager;

public class GCMParser 
{
	public boolean FlagOmitEmptyTokens = true;
	public boolean FlagTrimTokens = true;
	
	private List<String> errors = new ArrayList<String>();
	
	
	public List<String> getErrors()
	{
		return errors;
	}
	
	public String getAllErrorsAsString()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < errors.size(); i++)
			sb.append(errors.get(i) + "\n");
		return sb.toString();
	}
	
	public List<ILocalDescriptor> getLocalDescriptorsFromString(String locDescr)
	{
		return getLocalDescriptorsFromString(locDescr, ",");
	}
	
	public List<ILocalDescriptor> getLocalDescriptorsFromString(String locDescr, String separator)
	{
		errors.clear();
		List<ILocalDescriptor> descriptors = new ArrayList<ILocalDescriptor>();
		String tokens[] = locDescr.split(separator);
		for (int i = 0; i < tokens.length; i++ )
		{
			String tok;
			if (FlagTrimTokens)
				tok = tokens[i].trim();
			else
				tok = tokens[i];
						
			if (tok.equals(""))
			{	
				if (!FlagOmitEmptyTokens)
					errors.add("Emtpy local descriptor string");
				continue;
			}			
			
			ILocalDescriptor d = getLocalDescriptor(tok);
			if (d != null)
				descriptors.add(d);
		}
		
		return descriptors;
	}
	
	ILocalDescriptor getLocalDescriptor(String descrStr) 
	{
		List<ILocalDescriptor> predefined = LocalDescriptorManager.getPredefinedLocalDescriptors();
		for (ILocalDescriptor d : predefined)
		{
			if (d.getShortName().equals(descrStr))
			{
				try {
					ILocalDescriptor newDescr = d.getClass().newInstance();
					return newDescr;
				}
				catch(Exception e){
					errors.add(e.getMessage());
				}
			}
		}
		
		//Check for non-predefined
		//TODO
		
		errors.add("Unknown local descriptor: " + descrStr);
		return null;
	}
	
	public List<DescriptorInfo> getGlobalDescriptorsFromString(String globDescr)
	{
		return getGlobalDescriptorsFromString(globDescr, ",");
	}
	
	public List<DescriptorInfo> getGlobalDescriptorsFromString(String globDescr, String separator)
	{
		errors.clear();
		List<DescriptorInfo> descriptors = new ArrayList<DescriptorInfo>();
		String tokens[] = globDescr.split(separator);
		for (int i = 0; i < tokens.length; i++ )
		{
			String tok;
			if (FlagTrimTokens)
				tok = tokens[i].trim();
			else
				tok = tokens[i];
						
			if (tok.equals(""))
			{	
				if (!FlagOmitEmptyTokens)
					errors.add("Emtpy global descriptor string");
				continue;
			}			
			
			DescriptorInfo di = new DescriptorInfo();
			di.setName(tok);
			descriptors.add(di);
		}
		
		return descriptors;
	}
	
	
}
