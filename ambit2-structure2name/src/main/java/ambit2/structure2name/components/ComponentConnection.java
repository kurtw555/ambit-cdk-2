package ambit2.structure2name.components;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

public class ComponentConnection 
{
	public static enum ConType {
		ATOM_ATOM, SPIRO, COMMON_ATOM, BOND_FUSE, IS_SUB_COMPONENT, IS_SUPER_COMPONENT
	}
	
	public ConType type = ConType.ATOM_ATOM;
	public IIUPACComponent components[] = new IIUPACComponent[2];
	
	//connection elements
	public IAtom componentAtoms[] = new IAtom[2];	
	public IBond.Order connectionBondOrder = IBond.Order.SINGLE;
	public IBond fusedBond = null;
	
	/*	
	private int curAtomIndex = 0;
	private int connectedAtomIndex = 0;
	private IBond.Order connBondOrder = IBond.Order.SINGLE;
	private int curSpiroAtomIndex = 0;
	private int connectedSpiroAtomIndex = 0;
	private int curFusedBondIndex = 0;
	private int connectedFusedAtomIndex = 0;

	public ConType getType() {
		return type;
	}


	public void setType(ConType type) {
		this.type = type;
	}


	public IIUPACComponent getComponent() {
		return component;
	}


	public void setComponent(IIUPACComponent component) {
		this.component = component;
	}


	public int getCurAtomIndex() {
		return curAtomIndex;
	}


	public void setCurAtomIndex(int curAtomIndex) {
		this.curAtomIndex = curAtomIndex;
	}


	public int getConnectedAtomIndex() {
		return connectedAtomIndex;
	}


	public void setConnectedAtomIndex(int connectedAtomIndex) {
		this.connectedAtomIndex = connectedAtomIndex;
	}
	
	public IBond.Order getConnBondOrder() {
		return connBondOrder;
	}


	public void setConnBondOrder(IBond.Order connBondOrder) {
		this.connBondOrder = connBondOrder;
	}


	public int getCurSpiroAtomIndex() {
		return curSpiroAtomIndex;
	}


	public void setCurSpiroAtomIndex(int curSpiroAtomIndex) {
		this.curSpiroAtomIndex = curSpiroAtomIndex;
	}


	public int getConnectedSpiroAtomIndex() {
		return connectedSpiroAtomIndex;
	}


	public void setConnectedSpiroAtomIndex(int connectedSpiroAtomIndex) {
		this.connectedSpiroAtomIndex = connectedSpiroAtomIndex;
	}


	public int getCurFusedBondIndex() {
		return curFusedBondIndex;
	}


	public void setCurFusedBondIndex(int curFusedBondIndex) {
		this.curFusedBondIndex = curFusedBondIndex;
	}


	public int getConnectedFusedAtomIndex() {
		return connectedFusedAtomIndex;
	}


	public void setConnectedFusedAtomIndex(int connectedFusedAtomIndex) {
		this.connectedFusedAtomIndex = connectedFusedAtomIndex;
	}
	
	*/
	
}
