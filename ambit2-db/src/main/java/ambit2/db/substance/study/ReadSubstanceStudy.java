package ambit2.db.substance.study;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ambit2.base.data.I5Utils;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.ReliabilityParams;
import ambit2.db.search.AbstractQuery;
import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.query.QueryParam;
import net.idea.modbcum.q.conditions.EQCondition;

public class ReadSubstanceStudy<PA extends ProtocolApplication<Protocol, String, String, IParams, String>>
		extends AbstractQuery<String, PA, EQCondition, PA> implements IQueryRetrieval<PA> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1980335091441168568L;
	protected PA record = (PA) new ProtocolApplication<Protocol, String, String, IParams, String>(new Protocol(null));

	public void setRecord(PA record) {
		this.record = record;
	}

	private final static String sql = "SELECT document_prefix,hex(document_uuid) u,topcategory,endpointcategory,endpoint,guidance,substance_prefix,hex(substance_uuid) su,"
			+ "params,interpretation_result,interpretation_criteria,reference,reference_year,reference_owner,"
			+ "owner_prefix,hex(owner_uuid) ou,idsubstance,hex(rs_prefix),hex(rs_uuid) rsu,owner_name,"
			+ "reliability,isRobustStudy,isUsedforClassification,isUsedforMSDS,purposeFlag,studyResultType,if(investigation_uuid is null,null,hex(investigation_uuid)) as iuuid,if(assay_uuid is null,null,hex(assay_uuid)) as auuid,updated\n"
			+ "from substance_protocolapplication p\n"
			+ "left join substance s on s.prefix=p.substance_prefix and s.uuid=p.substance_uuid\n"
			+ "where substance_prefix =? and substance_uuid = unhex(?) ";

	private final static String whereTopCategory = "\nand topcategory=?";
	private final static String whereCategory = "\nand endpointcategory=?";
	private final static String whereProperty = "\nand document_uuid in (select document_uuid from substance_experiment where endpointhash =unhex(?))";
	private final static String whereInvestigation = "\nand investigation_uuid=unhex(?)";
	private final static String whereDocumentUUID = "\nand document_uuid=unhex(?)";

	@Override
	public String getSQL() throws AmbitException {
		if ((getValue() != null) && (getValue().getProtocol() != null)) {
			String wsql = sql;
			if (getValue().getProtocol().getTopCategory() != null)
				wsql += whereTopCategory;
			if (getValue().getProtocol().getCategory() != null)
				wsql += whereCategory;
			if (getValue().getEffects() != null && getValue().getEffects().get(0) != null
					&& getValue().getEffects().get(0).getSampleID() != null) {
				wsql += whereProperty;
			}
			if (getValue().getInvestigationUUID() != null)
				wsql += whereInvestigation;
			if (getValue().getDocumentUUID() != null)
				wsql += whereDocumentUUID;			
			

			return wsql;
		} else
			return sql;
	}

	@Override
	public List<QueryParam> getParameters() throws AmbitException {
		List<QueryParam> params = new ArrayList<QueryParam>();
		if (getFieldname() == null)
			throw new AmbitException("Empty substance id");
		String[] uuid = new String[] { null, getFieldname() };
		uuid = I5Utils.splitI5UUID(getFieldname());
		params.add(new QueryParam<String>(String.class, uuid[0]));
		params.add(new QueryParam<String>(String.class, uuid[1].replace("-", "").toLowerCase()));

		if ((getValue() != null) && (getValue().getProtocol() != null)) {
			if (getValue().getProtocol().getTopCategory() != null)
				params.add(new QueryParam<String>(String.class, getValue().getProtocol().getTopCategory()));
			if (getValue().getProtocol().getCategory() != null)
				params.add(new QueryParam<String>(String.class, getValue().getProtocol().getCategory()));
			if (getValue().getEffects() != null && getValue().getEffects().get(0) != null
					&& getValue().getEffects().get(0).getSampleID() != null) {
				params.add(new QueryParam<String>(String.class, getValue().getEffects().get(0).getSampleID()));
			}
		}
		if (getValue() != null && getValue().getInvestigationUUID() != null)
			params.add(new QueryParam<String>(String.class,
					getValue().getInvestigationUUID().toString().replace("-", "").toLowerCase()));
		
		if (getValue() != null && getValue().getDocumentUUID() != null)
			params.add(new QueryParam<String>(String.class,
					getValue().getDocumentUUID().toString().substring(4).replace("-", "").toLowerCase()));
		
		return params;
	}

	@Override
	public PA getObject(ResultSet rs) throws AmbitException {
		if (record == null) {
			record = (PA) new ProtocolApplication<Protocol, String, String, IParams, String>(new Protocol(null));
		} else
			record.clear();
		try {
			Protocol protocol = new Protocol(rs.getString("endpoint"));
			protocol.addGuideline(rs.getString("guidance"));
			protocol.setCategory(rs.getString("endpointcategory"));
			protocol.setTopCategory(rs.getString("topcategory"));
			record.setProtocol(protocol);
			try {
				record.setDocumentUUID(I5Utils.getPrefixedUUID(rs.getString("document_prefix"), rs.getString("u")));
			} catch (Exception xx) {
				record.setDocumentUUID(null);
			}
			try {
				record.setSubstanceUUID(I5Utils.getPrefixedUUID(rs.getString("substance_prefix"), rs.getString("su")));
			} catch (Exception xx) {
				record.setSubstanceUUID(null);
			}

			record.setReference(rs.getString("reference"));
			try {
				record.setReferenceYear(Integer.toString(rs.getInt("reference_year")));
			} catch (Exception x) {
				record.setReferenceYear(null);
			}
			try {
				record.setReferenceOwner(rs.getString("reference_owner"));
			} catch (Exception x) {
				record.setReferenceOwner(null);
			}
			record.setParameters(rs.getString("params")); // parse json
			record.setInterpretationCriteria(rs.getString("interpretation_criteria")); // parse
			// json
			record.setInterpretationResult(rs.getString("interpretation_result")); // parse
			// json

			record.setCompanyName(rs.getString("owner_name"));
			try {
				record.setCompanyUUID(I5Utils.getPrefixedUUID(rs.getString("owner_prefix"), rs.getString("ou")));
			} catch (Exception xx) {
				record.setCompanyUUID(null);
			}

			try {
				record.setReferenceSubstanceUUID(
						I5Utils.getPrefixedUUID(rs.getString("rs_prefix"), rs.getString("rsu")));

			} catch (Exception xx) {
				record.setReferenceSubstanceUUID(null);
			}
			ReliabilityParams reliability = new ReliabilityParams();
			record.setReliability(reliability);
			try {
				reliability.setValue(rs.getString("reliability"));
			} catch (Exception x) {
			}
			try {
				reliability.setIsRobustStudy(rs.getBoolean("isRobustStudy"));
			} catch (Exception x) {
			}
			try {
				reliability.setIsUsedforClassification(rs.getBoolean("isUsedforClassification"));
			} catch (Exception x) {
			}
			try {
				reliability.setIsUsedforMSDS(rs.getBoolean("isUsedforMSDS"));
			} catch (Exception x) {
			}
			try {
				reliability.setPurposeFlag(rs.getString("purposeFlag"));
			} catch (Exception x) {
			}
			try {
				reliability.setStudyResultType(rs.getString("studyResultType"));
			} catch (Exception x) {
			}

			try {
				String iuuid = rs.getString("iuuid");
				record.setInvestigationUUID(iuuid);
			} catch (Exception x) {
				record.setInvestigationUUID((String) null);
			}
			try {
				String auuid = rs.getString("auuid");
				record.setAssayUUID(auuid);
			} catch (Exception x) {
				record.setAssayUUID((String) null);
			}
			try {
				record.setUpdated(new Date(rs.getTimestamp("updated").getTime()));
			} catch (Exception x) {
				record.setReferenceYear(null);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return record;
	}

	@Override
	public boolean isPrescreen() {
		return false;
	}

	@Override
	public double calculateMetric(ProtocolApplication object) {
		return 1;
	}

}
