package ambit2.structure2name.components;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import ambit2.structure2name.components.ComponentUtils.Comparison;

public class Chain extends AbstractComponent 
{
	
	@Override
	public CompType getType() {
		return CompType.CHAIN;
	}
	

	/*
	  
	protected List<ComponentConnection> connections = new ArrayList<ComponentConnection>();
	
	protected int size = 1;
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}  
	
	@Override
	public IAtomContainer getSubstructure() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Integer> getIUPACAtomNumbering() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getDoubleBondIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getTripleBondIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getHeteroAtomIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ComponentConnection> getConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMainToken() {
		List<ComponentConnection> lesserConn = ComponentUtils.getConnections(connections, rank, Comparison.LESS_THAN);
		ComponentConnection sorted[] = ComponentUtils.sortConnectionsByRank(lesserConn);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < sorted.length; i++)
		{
			//TODO
		}
		
		return sb.toString();
	}
	*/

}
