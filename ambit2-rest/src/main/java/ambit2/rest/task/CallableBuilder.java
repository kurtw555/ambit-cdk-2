package ambit2.rest.task;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

import ambit2.rest.OpenTox;
import ambit2.rest.legacy.OTAlgorithm;
import ambit2.rest.legacy.OTAlgorithms;
import ambit2.rest.legacy.OTDataset;
import ambit2.rest.legacy.OTFeature;
import ambit2.rest.legacy.OTModel;

/**
 * 
<pre>
http://opentox.org/dev/apis/api-1.2/Algorithm

</pre>
 * @author nina
 *
 * @param <USERID>
 */
public class CallableBuilder<USERID> extends CallablePOST<USERID> {

	public CallableBuilder(Form form,Reference root,USERID token,String referer) throws Exception {
		this(MediaType.TEXT_URI_LIST,form.getWebRepresentation(),root,token,referer);
	}	
	

	public CallableBuilder(MediaType media, 
			  Representation input,
			  Reference root,
			  USERID token,String referer) {
		this(media,input,1500,root,token,referer);
	}
	public CallableBuilder(MediaType media, 
			  Representation input,
			  long pollInterval,
			  Reference root,
			  USERID token,String referer) {
		super(media,input,pollInterval,root,token,referer);
	}
	
	protected String getPredictionFeature(Form form) {
		String target = form.getFirstValue(OpenTox.params.target.toString());
		if (target!=null) target = target.trim();
		return target;
	}	
	
	@Override
	public TaskResult doCall() throws Exception {
		Form form = new Form(input);
		String dataset_service = getDatasetService(form);
		String dataset_uri = getDatasetURI(form);
		String target = getPredictionFeature(form);
		OTFeature prediction_feature = target == null?null:OTFeature.feature(target,referer);
		
		String[] feature_calculation = getAlgorithms(form, OpenTox.params.feature_calculation.toString());
		
		OTDataset dataset = dataset_uri!=null?OTDataset.dataset(dataset_uri).withDatasetService(dataset_service):null;
		if (target != null) {
			if (dataset == null) throw new Exception("No dataset!");
			dataset = dataset.removeColumns();
			dataset = dataset.addColumns(prediction_feature);
			dataset = dataset.copy();
		}
		
		
		//we have some descriptors to calculate before building a model
		if ((feature_calculation!=null) && (feature_calculation.length>0)) {
			if (dataset == null) throw new Exception("No dataset!");
			dataset = buildDataset(dataset.getUri().toString(), dataset_service, feature_calculation, form,referer);
		}
		
		String[] model_learning = getAlgorithms(form, OpenTox.params.model_learning.toString());
		if ((model_learning!=null) && (model_learning.length>0)) { //model
			OTAlgorithm algorithm = OTAlgorithm.algorithm(model_learning[0],"referer");
			try {
				OTModel model = algorithm.process(dataset, prediction_feature);
				return new TaskResult(model.getUri().toString());
			} catch (Exception x) {
				throw x;
			}
		} else {
			if (dataset==null)  throw new Exception("No dataset!");
			return new TaskResult(dataset.getUri().toString());
		}
	}
	
	protected OTDataset buildDataset(String datasetURI, 
				String dataset_service, 
				String[] feature_calculation,
				Form form,String referer) throws Exception {
		
		OTAlgorithms algorithms = OTAlgorithms.algorithms(null,referer);
		algorithms.withDatasetService(dataset_service);
		
		for (String algoUri : feature_calculation)
			if (algoUri!=null) 
				algorithms.add(OTAlgorithm.algorithm(algoUri.trim(),referer).withParams(form));
		
		return algorithms.process(OTDataset.dataset(datasetURI).withDatasetService(dataset_service),true);
	}
}
