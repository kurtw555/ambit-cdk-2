package ambit2.rest.model;

import java.awt.Dimension;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.data.model.Algorithm.AlgorithmFormat;
import ambit2.core.data.model.ModelQueryResults;
import ambit2.db.reporters.QueryTemplateReporter;
import ambit2.db.search.property.ModelTemplates;
import ambit2.db.update.model.AbstractModelQuery;
import ambit2.db.update.model.QueryModel;
import ambit2.db.update.model.ReadModel;
import ambit2.rest.DBConnection;
import ambit2.rest.DisplayMode;
import ambit2.rest.ImageConvertor;
import ambit2.rest.RDFJenaConvertor;
import ambit2.rest.algorithm.MLResources;
import ambit2.rest.model.predictor.AbstractStructureProcessor;
import ambit2.rest.model.predictor.DescriptorPredictor;
import ambit2.rest.model.predictor.ExpertModelpredictor;
import ambit2.rest.model.predictor.ExternalModelPredictor;
import ambit2.rest.model.predictor.FingerprintsPredictor;
import ambit2.rest.model.predictor.ModelPredictor;
import ambit2.rest.model.predictor.NumericADPredictor;
import ambit2.rest.model.predictor.Structure2DProcessor;
import ambit2.rest.model.predictor.StructureProcessor;
import ambit2.rest.model.task.CallableModelPredictor;
import ambit2.rest.property.PropertyURIReporter;
import ambit2.rest.query.ProcessingResource;
import ambit2.rest.query.QueryResource;
import ambit2.rest.task.CallableDescriptorCalculator;
import ambit2.rest.task.CallableQueryProcessor;
import ambit2.rest.task.CallableStructureOptimizer;
import ambit2.rest.task.tautomers.CallableTautomersGenerator;
import ambit2.rest.task.tautomers.TautomersGenerator;
import ambit2.rest.task.weka.CallableWekaPredictor;
import ambit2.rest.task.weka.FilteredWekaPredictor;
import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.exceptions.BatchProcessingException;
import net.idea.modbcum.i.exceptions.NotFoundException;
import net.idea.modbcum.q.conditions.StringCondition;
import net.idea.restnet.c.ChemicalMediaType;
import net.idea.restnet.c.RepresentationConvertor;
import net.idea.restnet.c.StringConvertor;
import net.idea.restnet.db.QueryURIReporter;
import net.idea.restnet.i.freemarker.IFreeMarkerApplication;

/**
 * Model as in http://opentox.org/development/wiki/Model Supported REST
 * operation: GET /model<br>
 * GET /model/{id}
 * 
 * @author nina
 * 
 */
public class ModelResource extends ProcessingResource<IQueryRetrieval<ModelQueryResults>, ModelQueryResults> {

	protected DisplayMode _dmode = DisplayMode.table;

	protected String category = "";

	public ModelResource() {
		super();
		getVariants().add(new Variant(MediaType.IMAGE_PNG));
		getVariants().add(new Variant(ChemicalMediaType.IMAGE_JSON));
		setHtmlbyTemplate(true);
	}

	@Override
	public String getTemplateName() {
		return "model.ftl";
	}

	protected Object getModelID(Object id) throws ResourceException {

		if (id != null)
			try {
				id = Reference.decode(id.toString());
				_dmode = DisplayMode.singleitem;
				return new Integer(id.toString());

			} catch (NumberFormatException x) {
				_dmode = DisplayMode.table;
				return id;
			} catch (Exception x) {
				return null;
			}
		else
			return null;

	}

	@Override
	protected IQueryRetrieval<ModelQueryResults> createQuery(Context context, Request request, Response response)
			throws ResourceException {
		Form form = getResourceRef(getRequest()).getQueryAsForm();
		AbstractModelQuery query = getModelQuery(
				getModelID(getRequest().getAttributes().get(MLResources.model_resourcekey)), form);

		return query;
	}

	@Override
	public RepresentationConvertor createConvertor(Variant variant) throws AmbitException, ResourceException {
		String filenamePrefix = getRequest().getResourceRef().getPath();
		if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
			return new StringConvertor(new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(getRequest()),
					MediaType.TEXT_URI_LIST);
		} else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
			return new StringConvertor(new ModelTextReporter<IQueryRetrieval<ModelQueryResults>>(getRequest()),
					MediaType.TEXT_PLAIN);
		} else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
			return new StringConvertor(new ModelJSONReporter<IQueryRetrieval<ModelQueryResults>>(getRequest(), null),
					MediaType.APPLICATION_JSON);
		} else if (variant.getMediaType().equals(MediaType.APPLICATION_JAVASCRIPT)) {
			Form params = getParams();
			String jsonpcallback = params.getFirstValue("jsonp");
			if (jsonpcallback == null)
				jsonpcallback = params.getFirstValue("callback");
			return new StringConvertor(
					new ModelJSONReporter<IQueryRetrieval<ModelQueryResults>>(getRequest(), jsonpcallback),
					MediaType.APPLICATION_JAVASCRIPT);
		} else if (variant.getMediaType().equals(MediaType.APPLICATION_RDF_XML)
				|| variant.getMediaType().equals(MediaType.APPLICATION_RDF_TURTLE)
				|| variant.getMediaType().equals(MediaType.TEXT_RDF_N3)
				|| variant.getMediaType().equals(MediaType.TEXT_RDF_NTRIPLES)
				|| variant.getMediaType().equals(ChemicalMediaType.APPLICATION_JSONLD)) {
			return new RDFJenaConvertor<ModelQueryResults, IQueryRetrieval<ModelQueryResults>>(
					new ModelRDFReporter<IQueryRetrieval<ModelQueryResults>>(getRequest(), variant.getMediaType()),
					variant.getMediaType(), filenamePrefix);
		} else if (variant.getMediaType().equals(MediaType.IMAGE_PNG)
				|| variant.getMediaType().equals(MediaType.IMAGE_BMP)
				|| variant.getMediaType().equals(MediaType.IMAGE_JPEG)
				|| variant.getMediaType().equals(MediaType.IMAGE_TIFF)
				|| variant.getMediaType().equals(MediaType.IMAGE_GIF)
				|| variant.getMediaType().equals(ChemicalMediaType.IMAGE_JSON)) {
			Dimension d = new Dimension(250, 250);
			Form form = getRequest().getResourceRef().getQueryAsForm();
			try {

				d.width = Integer.parseInt(form.getFirstValue("w").toString());
			} catch (Exception x) {
			}
			try {
				d.height = Integer.parseInt(form.getFirstValue("h").toString());
			} catch (Exception x) {
			}

			return new ImageConvertor<ModelQueryResults, IQueryRetrieval<ModelQueryResults>>(
					new ModelImageReporter(getRequest(), getResourceRef(getRequest()).getQueryAsForm(), d),
					variant.getMediaType());
		} else
			// html
			return new StringConvertor(new ModelJSONReporter<IQueryRetrieval<ModelQueryResults>>(getRequest(), null),
					MediaType.APPLICATION_JSON);
	}

	@Override
	protected QueryURIReporter<ModelQueryResults, IQueryRetrieval<ModelQueryResults>> getURUReporter(
			Request baseReference) throws ResourceException {
		return new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(getRequest());
	}

	protected void readVariables(ModelQueryResults model) throws ResourceException {

		ModelTemplates query = new ModelTemplates();
		query.setFieldname(model);

		QueryTemplateReporter<ModelTemplates> reporter = null;
		Connection connection = null;
		try {
			DBConnection dbc = new DBConnection(getContext());
			connection = dbc.getConnection();

			query.setValue("dependent");
			reporter = new QueryTemplateReporter<ModelTemplates>(model.getDependent());
			reporter.setCloseConnection(false);
			reporter.setConnection(connection);
			// dependent
			try {
				reporter.process(query);
			} catch (BatchProcessingException x) {
				if (x.getCause() instanceof NotFoundException) {
					// this is ok
				} else
					throw x;
			}

			query.setValue("independent");
			reporter.setConnection(connection);
			reporter.setOutput(model.getPredictors());
			try {
				reporter.process(query);
			} catch (BatchProcessingException x) {
				if (x.getCause() instanceof NotFoundException) {
					// this is ok
				} else
					throw x;
			}

			query.setValue("predicted");
			reporter.setConnection(connection);
			reporter.setOutput(model.getPredicted());
			try {
				reporter.process(query);
			} catch (BatchProcessingException x) {
				if (x.getCause() instanceof NotFoundException) {
					// ok, it may change the structure only
				} else
					throw x;

			}

		} catch (Exception x) {
			getLogger().log(Level.FINE, x.getMessage(), x);
		}

		try {
			reporter.setCloseConnection(true);
		} catch (Exception x) {
			getLogger().log(Level.WARNING, x.getMessage(), x);
		} finally {
			try {
				reporter.close();
			} catch (Exception x) {
			}
		}
		try {
			connection.close();
		} catch (Exception x) {
		}

	}

	@Override
	protected CallableQueryProcessor createCallable(Form form, ModelQueryResults model) throws ResourceException {

		try {
			readVariables(model);
			Object token = getToken();

			final ModelPredictor thepredictor = ModelResource.getPredictor(model, getRequest());

			if (model.getContentMediaType().equals(AlgorithmFormat.WWW_FORM.getMediaType())) {

				if (thepredictor instanceof ExpertModelpredictor)
					return new CallableModelPredictor<IStructureRecord, ExpertModelpredictor, Object>(form,
							getRequest().getRootRef(), getContext(), (ExpertModelpredictor) thepredictor, token,
							getRequest().getResourceRef().toString(),getClientInfo()) {
						@Override
						protected void processForm(Reference applicationRootReference, Form form) {
							super.processForm(applicationRootReference, form);
							((ExpertModelpredictor) thepredictor).setValue(form.getFirstValue("value"));
						};
					};
				else
					return new CallableModelPredictor<IStructureRecord, ModelPredictor, Object>(form,
							getRequest().getRootRef(), getContext(), thepredictor, token,
							getRequest().getResourceRef().toString(),getClientInfo());
			} else if (model.getContentMediaType().equals(AlgorithmFormat.WEKA.getMediaType())) {
				return // reads Instances, instead of IStructureRecord
				new CallableWekaPredictor<Object, Object>(form, getRequest().getRootRef(), getContext(), thepredictor,
						token, getRequest().getResourceRef().toString(),getClientInfo());
				/*
				 * } else if (model.getContentMediaType().equals(
				 * 
				 * AlgorithmFormat.WAFFLES_JSON.getMediaType())) { return new
				 * CallableWafflesPredictor(form, getRequest() .getRootRef(),
				 * getContext(), (WafflesPredictor) thepredictor,
				 * token,getRequest().getResourceRef().toString());
				 */
			} else if (model.getContentMediaType().equals(AlgorithmFormat.COVERAGE_SERIALIZED.getMediaType())) {

				if (model.getPredictors().size() == 0) { // hack for structure
															// based AD
					if (thepredictor instanceof FingerprintsPredictor)
						return new CallableModelPredictor<IStructureRecord, FingerprintsPredictor, Object>(form,
								getRequest().getRootRef(), getContext(), (FingerprintsPredictor) thepredictor, token,
								getRequest().getResourceRef().toString(),getClientInfo());
					else
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
								String.format("Not supported %s", thepredictor.getClass().getName()));
				} else {
					return new CallableModelPredictor(form, getRequest().getRootRef(), getContext(), thepredictor,
							token, getRequest().getResourceRef().toString(),getClientInfo());
					/*
					 * return new CallableWekaPredictor<DataCoverage>( //reads
					 * Instances, instead of IStructureRecord form,
					 * getRequest().getRootRef(), getContext(), predictor) ;
					 */
				}
			} else if (model.getContentMediaType().equals(AlgorithmFormat.Structure2D.getMediaType())) {
				return new CallableStructureOptimizer(form, getRequest().getRootRef(), getContext(),
						(Structure2DProcessor) thepredictor, token, getRequest().getResourceRef().toString(),getClientInfo());
			} else if (model.getContentMediaType().equals(AlgorithmFormat.MOPAC.getMediaType())) {
				return new CallableStructureOptimizer(form, getRequest().getRootRef(), getContext(),
						(StructureProcessor) thepredictor, token, getRequest().getResourceRef().toString(),getClientInfo());
			} else if (model.getContentMediaType().equals(AlgorithmFormat.TAUTOMERS.getMediaType())) {
				return new CallableTautomersGenerator(form, getRequest().getRootRef(), getContext(),
						(TautomersGenerator) thepredictor, token, getRequest().getResourceRef().toString(),getClientInfo());

			} else if (model.getContentMediaType().equals(AlgorithmFormat.JAVA_CLASS.getMediaType())) {
				return new CallableDescriptorCalculator(form, getRequest().getRootRef(), getContext(),
						(DescriptorPredictor) thepredictor, token, getRequest().getResourceRef().toString(),getClientInfo());
			} else
				throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, model.getContentMediaType());
		} catch (ResourceException x) {
			throw x;
		} catch (Exception x) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, x.getMessage(), x);
		} finally {

		}
	}

	protected AbstractModelQuery getModelQuery(Object idmodel, Form form) throws ResourceException {
		_dmode = DisplayMode.singleitem;
		AbstractModelQuery query = null;
		ModelQueryResults model_query = null;
		String algorithm = form.getFirstValue(AbstractModelQuery._models_criteria.algorithm.name());
		if (algorithm != null) {
			model_query = new ModelQueryResults();
			model_query.setCreator(null);
			model_query.setAlgorithm(algorithm);
		}
		String dataset = form.getFirstValue(AbstractModelQuery._models_criteria.dataset.name());
		if (dataset != null) {
			if (model_query == null) {
				model_query = new ModelQueryResults();
				model_query.setCreator(null);
			}
			model_query.setTrainingInstances(dataset);
		}
		String creator = form.getFirstValue(AbstractModelQuery._models_criteria.creator.name());
		if (creator != null) {
			if (model_query == null) {
				model_query = new ModelQueryResults();
			}
			model_query.setCreator(creator);
		}

		String endpoint = form.getFirstValue(AbstractModelQuery._models_criteria.endpoint.name());
		if (endpoint != null) {
			if (model_query == null) {
				model_query = new ModelQueryResults();
				model_query.setEndpoint(null);
			}

			model_query.setEndpoint(endpoint);
		}

		if (model_query == null) {
			query = new ReadModel();
			String condition = form.getFirstValue(QueryResource.condition);
			if (condition != null)
				query.setCondition(StringCondition.getInstance(condition));

			String name = form.getFirstValue(QueryResource.search_param);
			if (name != null) {
				query.setFieldname(name.trim());
				if (condition == null)
					query.setCondition(StringCondition.getInstance(StringCondition.C_STARTS_WITH));
			}

			if (idmodel == null) {
				_dmode = DisplayMode.table;
				return query;
			} else if (idmodel instanceof Integer) {
				_dmode = DisplayMode.singleitem;
				query.setValue((Integer) idmodel);
				return query;
			} else {
				_dmode = DisplayMode.table;
				query.setFieldname(idmodel.toString());
				return query;
			}
		} else {
			String name = form.getFirstValue(QueryResource.search_param);
			if (name != null)
				model_query.setName(name);
			if (idmodel != null)
				if (idmodel instanceof Integer)
					model_query.setId((Integer) idmodel);
				else
					model_query.setName(idmodel.toString());

			return new QueryModel(model_query);
		}
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {

		return super.post(entity, variant);
	}

	@Override
	public void configureTemplateMap(Map<String, Object> map, Request request, IFreeMarkerApplication app) {
		super.configureTemplateMap(map, request, app);
		Object modelid = getRequest().getAttributes().get(MLResources.model_resourcekey);
		if (modelid != null)
			map.put("modelid", modelid.toString());
	}

	public static ModelPredictor getPredictor(ModelQueryResults model, Request request) throws ResourceException {

		try {

			if (model.getContentMediaType().equals(AlgorithmFormat.WEKA.getMediaType())) {
				return new FilteredWekaPredictor(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request));
				/*
				 * } else if (model.getContentMediaType().equals(
				 * AlgorithmFormat.WAFFLES_JSON.getMediaType())) { return new
				 * WafflesPredictor( request.getRootRef(), model, new
				 * ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(
				 * request));
				 */
			} else if (model.getContentMediaType().equals(AlgorithmFormat.COVERAGE_SERIALIZED.getMediaType())) {
				if (model.getPredictors().size() == 0) { // hack for structure
															// based AD
					return new FingerprintsPredictor(request.getRootRef(), model,
							new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request), null, null);

				} else {
					return new NumericADPredictor(request.getRootRef(), model,
							new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
							new PropertyURIReporter(request), null);

				}
			} else if (model.getContentMediaType().equals(AlgorithmFormat.PREFERRED_STRUC.getMediaType())) {
				return new AbstractStructureProcessor(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
						new PropertyURIReporter(request), null) {

					@Override
					public boolean isStructureRequired() {
						return true;
					}

				};
			} else if (model.getContentMediaType().equals(AlgorithmFormat.Structure2D.getMediaType())) {

				return new Structure2DProcessor(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
						new PropertyURIReporter(request), null);
			} else if (model.getContentMediaType().equals(AlgorithmFormat.MOPAC.getMediaType())) {

				return new StructureProcessor(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
						new PropertyURIReporter(request), null);
			} else if (model.getContentMediaType().equals(AlgorithmFormat.TAUTOMERS.getMediaType())) {

				return new TautomersGenerator(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
						new PropertyURIReporter(request), null);
			} else if (model.getContentMediaType().equals(AlgorithmFormat.JAVA_CLASS.getMediaType())) {

				return new DescriptorPredictor(request.getRootRef(), model,
						new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
						new PropertyURIReporter(request), null);

			} else if (model.getContentMediaType().equals(AlgorithmFormat.WWW_FORM.getMediaType())) {
				if (model.getContent().equals("expert"))
					return new ExpertModelpredictor(request.getRootRef(), model,
							new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
							new PropertyURIReporter(request), null

					);
				else
					return new ExternalModelPredictor(request.getRootRef(), model,
							new ModelURIReporter<IQueryRetrieval<ModelQueryResults>>(request),
							new PropertyURIReporter(request), null);

			} else
				throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, model.getContentMediaType());
		} catch (ResourceException x) {
			throw x;
		} catch (Exception x) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, x.getMessage(), x);
		} finally {

		}
	}

}
