package ambit2.db.substance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ambit2.base.data.I5Utils;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.db.substance.study.facet.ProtocolApplicationSummaryFacet;
import ambit2.db.substance.study.facet.SubstanceByCategoryFacet;
import ambit2.db.substance.study.facet.SubstanceDataAvailabilityFacet;
import ambit2.db.update.dataset.QueryCount;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.query.QueryParam;

public class QueryCountProtocolApplications<F extends SubstanceByCategoryFacet> extends QueryCount<F> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6001454887114379950L;

	public enum _mode_related {
		endpoints, substances, detail, data_availability
	}

	public _mode_related mode = _mode_related.endpoints;

	public _mode_related getMode() {
		return mode;
	}

	public void setMode(_mode_related mode) {
		this.mode = mode;
	}

	protected SubstanceEndpointsBundle bundle = null;

	public SubstanceEndpointsBundle getBundle() {
		return bundle;
	}

	public void setBundle(SubstanceEndpointsBundle bundle) {
		this.bundle = bundle;
	}

	public QueryCountProtocolApplications(String facetURL) {
		this(facetURL, _mode_related.endpoints);
	}

	public QueryCountProtocolApplications(String facetURL, _mode_related mode) {
		super(facetURL);
		setPageSize(Integer.MAX_VALUE);
		setMode(mode);
		facet = createFacet(facetURL);
	}

	/**
	 * 
	 */

	private static final String sql = "SELECT topcategory,count(*),endpointcategory,-1  FROM substance_protocolapplication p %s group by topcategory,endpointcategory";

	private static final String sql_detail = "SELECT topcategory,count(*),endpointcategory,-1,guidance,interpretation_result  FROM substance_protocolapplication p %s group by topcategory,endpointcategory,guidance,interpretation_result";

	private static final String sql_bundle_byendpoints = "SELECT p.topcategory,count(*),p.endpointcategory,-1  FROM substance_protocolapplication p join bundle_endpoints b where b.topcategory=p.topcategory and b.endpointcategory=p.endpointcategory and idbundle=? %s group by p.topcategory,p.endpointcategory";

	private static final String sql_bundle_bysubstances = "select p.topcategory,count(*),p.endpointcategory,-1 from substance_protocolapplication p join bundle_substance using(substance_prefix,substance_uuid)\n"
			+ "where %s idbundle=? group by p.topcategory,p.endpointcategory";

	private static final String sql_dataavailability = "select topcategory,c,endpointcategory,-1,prefix as substance_prefix,hex(uuid) as uuid,protocol,provider,reference,name,publicname,owner_name,substancetype from substance join ("
			+ "SELECT topcategory,count(*) c,endpointcategory,-1,substance_prefix as prefix,substance_uuid as uuid,group_concat(distinct(guidance)) as protocol,group_concat(distinct(reference_owner)) provider ,group_concat(distinct(reference)) as reference FROM substance_protocolapplication group by substance_prefix,substance_uuid,topcategory,endpointcategory "
			+ "order by substance_prefix,substance_uuid,topcategory,endpointcategory " + ") a using (prefix,uuid)";
	/*
	 * select p.topcategory,p.endpointcategory,count(*) from
	 * substance_protocolapplication p, bundle_endpoints b where
	 * p.topcategory=b.topcategory and p.endpointcategory=b.endpointcategory and
	 * idbundle=1 group by p.topcategory,p.endpointcategory order by
	 * p.topcategory,p.endpointcategory
	 */
	private static final String w_topcategory = "p.topcategory=?";
	private static final String w_endpointcategory = "p.endpointcategory=?";

	@Override
	public String getSQL() throws AmbitException {
		StringBuilder b = new StringBuilder();
		switch (mode) {
		case data_availability: {
			return sql_dataavailability;
		}
		case detail: {
			String d = "";
			if (getFieldname() != null) {
				b.append(d);
				b.append(w_topcategory);
				d = " and ";
			}
			if (getValue() != null) {
				b.append(d);
				b.append(w_endpointcategory);
				d = " and ";
			}
			return String.format(sql_detail, b.toString());
		}
		case substances: {
			if ((bundle != null) && (bundle.getID() > 0)) {
				String d = " and ";
				if (getFieldname() != null) {
					b.append(w_topcategory);
					b.append(d);
				}
				if (getValue() != null) {
					b.append(w_endpointcategory);
					b.append(d);
				}
				return String.format(sql_bundle_bysubstances, b.toString());
			} else
				throw new AmbitException("bundle not defined");
		}
		default: {
			String d = ((bundle != null) && (bundle.getID() > 0)) ? "\nand " : "\nwhere ";
			if (getFieldname() != null) {
				b.append(d);
				b.append(w_topcategory);
				d = " and ";
			}
			if (getValue() != null) {
				b.append(d);
				b.append(w_endpointcategory);
				d = " and ";
			}
			return String.format(((bundle != null) && (bundle.getID() > 0)) ? sql_bundle_byendpoints : sql,
					b.toString());
		}
		}

	}

	@Override
	public void setValue(String value) {
		if (value != null && !value.endsWith("_SECTION"))
			super.setValue(value + "_SECTION");
		else
			super.setValue(value);
	}

	@Override
	protected F createFacet(String facetURL) {
		if (mode == null)
			return (F) new SubstanceByCategoryFacet(facetURL);
		F facet;

		switch (mode) {
		case data_availability: {
			facet = (F) new SubstanceDataAvailabilityFacet(facetURL);
			break;
		}
		case detail: {
			facet = (F) new ProtocolApplicationSummaryFacet(facetURL);
			break;
		}
		default: {
			facet = (F) new SubstanceByCategoryFacet(facetURL);
			break;
		}
		}
		return facet;

	}

	@Override
	public List<QueryParam> getParameters() throws AmbitException {
		List<QueryParam> params = new ArrayList<QueryParam>();
		switch (mode) {
		case substances: {
			if (getFieldname() != null)
				params.add(new QueryParam<String>(String.class, getFieldname().toString()));
			if (getValue() != null)
				params.add(new QueryParam<String>(String.class, getValue().toString()));
			if ((bundle != null) && (bundle.getID() > 0))
				params.add(new QueryParam<Integer>(Integer.class, getBundle().getID()));
			else
				throw new AmbitException("bundle not defined");
			return params;
		}
		default: {
			if ((bundle != null) && (bundle.getID() > 0))
				params.add(new QueryParam<Integer>(Integer.class, getBundle().getID()));
			if (getFieldname() != null)
				params.add(new QueryParam<String>(String.class, getFieldname().toString()));
			if (getValue() != null)
				params.add(new QueryParam<String>(String.class, getValue().toString()));
			return params;
		}
		}
	}

	@Override
	public F getObject(ResultSet rs) throws AmbitException {
		try {
			facet.setSubcategoryTitle(rs.getString(1));
			facet.setValue(rs.getString(3));
			facet.setCount(rs.getInt(2));
			facet.setSubstancesCount(rs.getInt(4));

			switch (mode) {
			case data_availability: {
				try {
					SubstanceDataAvailabilityFacet ps = (SubstanceDataAvailabilityFacet) facet;
					SubstanceRecord record = new SubstanceRecord();

					record.setSubstanceUUID(
							I5Utils.getPrefixedUUID(rs.getString("substance_prefix"), rs.getString("uuid")));
					record.setSubstanceName(rs.getString("name"));
					record.setPublicName(rs.getString("publicname"));
					record.setSubstancetype(rs.getString("substancetype"));
					record.setOwnerName(rs.getString("owner_name"));
					Protocol p = new Protocol(null,rs.getString("protocol"));
					p.setTopCategory(rs.getString("topcategory"));
					p.setCategory(rs.getString("endpointcategory"));
					ProtocolApplication papp = new ProtocolApplication<Protocol, Object, String, Object, String>(p);
					papp.setReference(rs.getString("reference"));
					papp.setReferenceOwner(rs.getString("provider"));
					List<ProtocolApplication> papps = new ArrayList<ProtocolApplication>();
					papps.add(papp);
					record.setMeasurements(papps);
					ps.setRecord(record);
				} catch (Exception x) {
					x.printStackTrace();
				}
				break;
			}
			case detail:
				try {
					ProtocolApplicationSummaryFacet ps = (ProtocolApplicationSummaryFacet) facet;
					ps.setInterpretation_result(rs.getString("interpretation_result"));
					ps.setGuidance(rs.getString("guidance"));
				} catch (Exception x) {
				}
				break;
			}
			return facet;
		} catch (SQLException x) {
			throw new AmbitException(x);
		}
	}

}
