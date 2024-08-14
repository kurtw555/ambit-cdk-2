package ambit2.rest.model.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.xerces.impl.dv.util.Base64;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.PredictedVarsTemplate;
import ambit2.base.data.Property;
import ambit2.base.data.Template;
import ambit2.core.data.model.Algorithm;
import ambit2.core.data.model.Algorithm.AlgorithmFormat;
import ambit2.core.data.model.AlgorithmType;
import ambit2.core.data.model.ModelQueryResults;
import ambit2.descriptors.processors.DescriptorsFactory;
import ambit2.rest.algorithm.AlgorithmURIReporter;
import ambit2.rest.model.ModelURIReporter;
import net.idea.modbcum.i.exceptions.AmbitException;

/**
 * Builds a model from ModelQueryResults. The model is based on
 * IMolecularDescriptor class, without input variables.
 * 
 * @author nina
 *
 * @param <A>
 * @param <Model>
 */
public class SimpleModelBuilder extends ModelBuilder<Object, Algorithm, ModelQueryResults> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2827461619547962205L;
	protected boolean modelHidden = false;
	protected String[] parameters = null;

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	public SimpleModelBuilder(Reference applicationRootReference, ModelURIReporter model_reporter,
			AlgorithmURIReporter alg_reporter, String referer) {
		this(applicationRootReference, model_reporter, alg_reporter, false, referer);
	}

	public SimpleModelBuilder(Reference applicationRootReference, ModelURIReporter model_reporter,
			AlgorithmURIReporter alg_reporter, boolean isModelHidden, String referer) {
		super(applicationRootReference, model_reporter, alg_reporter, referer);
		this.modelHidden = isModelHidden;
	}

	protected List<Property> createProperties(Algorithm algorithm) throws Exception {
		return DescriptorsFactory.createDescriptor2Properties(algorithm.getContent().toString());
	}

	protected String getMediaType(Algorithm algorithm) throws AmbitException {
		if (algorithm.hasType(AlgorithmType.ExternalModels))
			return algorithm.getFormat().getMediaType();
		else
			return AlgorithmFormat.JAVA_CLASS.getMediaType();
	}

	protected String getContent(Algorithm algorithm) throws AmbitException {
		if (algorithm.hasType(AlgorithmType.ExternalModels)) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
					oos.writeObject(algorithm.getContent());
					oos.flush();
				} catch (Exception x) {
				}

				byte[] content = out.toByteArray();
				
				Form form = new Form();
				form.add("model", Base64.encode(content));
				form.add("objecttype", AlgorithmFormat.JAVA_CLASS.name());
				
				return form.getWebRepresentation().getText();
			} catch (IOException x) {
				throw new AmbitException(x);
			}
		} else
			return algorithm.getContent().toString();

	}

	protected String getName(Algorithm algorithm) throws AmbitException {
		return algorithm.getName();
	}

	protected ModelQueryResults createModel(Algorithm algorithm) throws AmbitException {
		ModelQueryResults mr = new ModelQueryResults();
		mr.setHidden(modelHidden);
		mr.setContentMediaType(getMediaType(algorithm));
		mr.setName(getName(algorithm));
		mr.setContent(getContent(algorithm));
		mr.setAlgorithm(alg_reporter.getURI(algorithm));
		mr.setPredictors(algorithm.getInput());
		if (parameters != null) {
			mr.setParameters(parameters);
			StringBuilder b = new StringBuilder();
			for (String param : parameters) {
				b.append(param);
				b.append(",");
			}
			mr.setName(UUID.nameUUIDFromBytes(b.toString().getBytes()) + "-" + mr.getName());
		}
		return mr;
	}

	protected PredictedVarsTemplate createPredictedTemplate(Algorithm algorithm) throws Exception {
		PredictedVarsTemplate predicted = new PredictedVarsTemplate(String.format("%s", algorithm.getName()));
		return predicted;
	}

	public ModelQueryResults process(Algorithm algorithm) throws AmbitException {
		try {
			List<Property> p = createProperties(algorithm);
			if ((p == null) || (p.size() == 0))
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Can't create a model from " + algorithm);

			ModelQueryResults mr = createModel(algorithm);

			Template dependent = new Template("Empty");
			mr.setDependent(dependent);

			PredictedVarsTemplate predicted = createPredictedTemplate(algorithm);
			mr.setPredicted(predicted);

			for (Property property : p) {
				property.setEnabled(true);
				if (property.getName() == null)
					continue;
				predicted.add(property);
				if (algorithm.getEndpoint() != null)
					property.setLabel(algorithm.getEndpoint());
				property.getReference().setType(modelHidden ? _type.Algorithm : _type.Model);
			}

			return mr;
		} catch (AmbitException x) {
			throw x;
		} catch (Exception x) {

			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
					x.getCause() == null ? x.getMessage() : x.getCause().getMessage(), x);
		}
	}
}
