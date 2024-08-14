package ambit2.rest.task;

import java.io.ObjectInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import ambit2.db.DbReaderStructure;
import ambit2.db.search.structure.AbstractStructureQuery;
import ambit2.rest.DBConnection;
import ambit2.rest.OpenTox;
import ambit2.rest.dataset.RDFStructuresReader;
import ambit2.rest.legacy.OTDataset;
import ambit2.rest.legacy.OTFeature;
import net.idea.modbcum.i.batch.IBatchStatistics;
import net.idea.modbcum.i.processors.IProcessor;
import net.idea.modbcum.i.processors.ProcessorsChain;
import net.idea.modbcum.p.batch.AbstractBatchProcessor;
import net.idea.restnet.c.task.ClientResourceWrapper;

public abstract class CallableQueryProcessor<Target, Result, USERID> extends CallableProtectedTask<USERID> {
	protected AbstractBatchProcessor batch;
	protected Target target;
	protected Reference sourceReference;
	// protected AmbitApplication application;
	protected Context context;
	protected String referer;
	protected String agent;

	public CallableQueryProcessor(Form form, Context context, USERID token, String referer, ClientInfo clientinfo) {
		this(null, form, context, token, referer,clientinfo);
	}

	public CallableQueryProcessor(Reference applicationRootReference, Form form, Context context, USERID token,
			String referer,ClientInfo clientinfo) {
		super(token);
		processForm(applicationRootReference, form);
		this.context = context;
		this.referer = referer;
		this.agent=clientinfo==null?null:clientinfo.getAgent();

	}
	
	public String getCookies() {
		if (getToken() instanceof Cookie) {
			Cookie cookie = (Cookie) getToken();
			return String.format("%s=%s", cookie.getName(),cookie.getValue());
		}
		return null;
	}

	public String getAgent() {
		return agent;
	}

	protected void processForm(Reference applicationRootReference, Form form) {
		Object dataset = OpenTox.params.dataset_uri.getFirstValue(form);
		String[] xvars = OpenTox.params.feature_uris.getValuesArray(form);
		if (xvars != null)
			try {

				OTDataset ds = OTDataset.dataset(dataset.toString());
				for (String xvar : xvars) {
					String[] xx = xvar.split("\n");
					for (String x : xx)
						if (!x.trim().equals(""))
							ds = ds.addColumns(OTFeature.feature(x, referer));
				}
				dataset = ds.getUri().toString();

			} catch (Exception x) {

			}
		this.sourceReference = dataset == null ? null : new Reference(dataset.toString().trim());
	}

	@Override
	public TaskResult doCall() throws Exception {

		Context.getCurrentLogger().fine("Start()");
		Connection connection = null;
		try {
			DBConnection dbc = new DBConnection(context);
			connection = dbc.getConnection();
			try {
				target = createTarget(sourceReference);
			} catch (Exception x) {
				target = (Target) sourceReference;
			}

			batch = createBatch(target);

			if (batch != null) {
				batch.setCloseConnection(false);
				batch.setProcessorChain(createProcessors());
				try {
					if ((connection == null) || connection.isClosed())
						throw new Exception("SQL Connection unavailable ");
					batch.setConnection(connection);
					batch.open();
				} catch (Exception x) {
					connection = null;
				}
				/*
				 * batch.addPropertyChangeListener(AbstractBatchProcessor.
				 * PROPERTY_BATCHSTATS,new PropertyChangeListener(){ public void
				 * propertyChange(PropertyChangeEvent evt) {
				 * context.getLogger().info(evt.getNewValue().toString());
				 * 
				 * } });
				 */
				IBatchStatistics stats = runBatch(target);
			}
			return createReference(connection);
		} catch (Exception x) {
			Context.getCurrentLogger().log(Level.SEVERE, x.getMessage(), x);

			throw x;
		} finally {
			Context.getCurrentLogger().fine("Done");
			try {
				connection.close();
			} catch (Exception x) {
				Context.getCurrentLogger().warning(x.getMessage());
			}
		}
		/*
		 * try { //connection = application.getConnection((Request)null); //if
		 * (connection.isClosed()) connection =
		 * application.getConnection((Request)null); return
		 * createReference(connection); } catch (Exception x) {
		 * x.printStackTrace(); throw x; } finally { try { connection.close(); }
		 * catch (Exception x) {} }
		 */

	}

	protected IBatchStatistics runBatch(Target target) throws Exception {
		return batch.process(target);
	}

	protected AbstractBatchProcessor createBatch(Target target) throws Exception {
		if (target == null)
			throw new Exception("");
		if (target instanceof AbstractStructureQuery) {
			DbReaderStructure reader = new DbReaderStructure();
			reader.setHandlePrescreen(true);
			return reader;
		} else
			return new RDFStructuresReader(target.toString(), referer);
	}

	protected abstract Target createTarget(Reference reference) throws Exception;

	protected abstract TaskResult createReference(Connection connection) throws Exception;

	protected abstract ProcessorsChain<Result, IBatchStatistics, IProcessor> createProcessors() throws Exception;

	// protected abstract QueryURIReporter createURIReporter(Request request);
	/*
	 * public static Object getQueryObjectNoCookies(Reference reference,
	 * Reference applicationRootReference, Context context, String referer)
	 * throws Exception { return getQueryObject(reference,
	 * applicationRootReference, context, null, null, referer); }
	 */
	public static Object getQueryObject(Reference reference, Reference applicationRootReference, Context context,
			Series<Cookie> cookies, String agent, String referer) throws Exception {
		StringBuilder cookiestring = null;
		if (cookies != null)
			for (Cookie cookie : cookies) {
				if (cookiestring == null)
					cookiestring = new StringBuilder();
				else
					cookiestring.append(";");
				cookiestring.append(cookie.getName());
				cookiestring.append("=");
				cookiestring.append(cookie.getValue());
			}
		return getQueryObject(reference, applicationRootReference, context, cookiestring.toString(), agent, referer);
	}

	public static Object getQueryObject(Reference reference, Reference applicationRootReference, Context context,
			String cookies, String agent, String referer) throws Exception {
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
		Reference ref = reference.clone();
		ref.setQuery("");
		if (!applicationRootReference.isParent(ref)) {
			logger.log(Level.WARNING, String.format("Remote reference %s %s", applicationRootReference, ref));
			return null;
		}

		HttpURLConnection connection = null;
		try {
			connection = ClientResourceWrapper.getHttpURLConnection(reference.toString(), "GET",
					MediaType.APPLICATION_JAVA_OBJECT.toString(), referer);
			HttpURLConnection.setFollowRedirects(true);
			if (agent != null)
				connection.setRequestProperty("User-Agent", agent);
			if (cookies != null)
				connection.setRequestProperty("Cookie", cookies);
			switch (connection.getResponseCode()) {
			case HttpURLConnection.HTTP_OK: {
				try (ObjectInputStream in = new ObjectInputStream(connection.getInputStream())) {
					Object object = in.readObject();
					return object;
				}
			}
			case HttpURLConnection.HTTP_FORBIDDEN: {
				throw new ResourceException(connection.getResponseCode());
			}
			case HttpURLConnection.HTTP_UNAUTHORIZED: {
				throw new ResourceException(connection.getResponseCode());
			}
			}

			return reference;
		} catch (Exception x) {
			throw x;
		} finally {
			try {
				if (connection != null)
					connection.disconnect();
			} catch (Exception x) {
			}
		}
	}

}
