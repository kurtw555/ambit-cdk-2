package ambit2.base.json;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * http://www.ietf.org/rfc/rfc4627.txt
 * 
 * @author nina
 * 
 */
public class JSONUtils {
	private static final ThreadLocal<SimpleDateFormat> datef = new ThreadLocal<SimpleDateFormat>(){
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy");
		}
	};

	private static final ThreadLocal<NumberFormat> nf = new ThreadLocal<NumberFormat>() {
		@Override
		protected NumberFormat initialValue() {
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
			nf.setMaximumFractionDigits(4);
			nf.setGroupingUsed(false);
			return nf;
		}
	};
	private static final ThreadLocal<DecimalFormat> df = new ThreadLocal<DecimalFormat>() {
		@Override
		protected DecimalFormat initialValue() {
			DecimalFormat df = new DecimalFormat("0.000E00");
			df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
			return df;
		}
	};

	public static String jsonEscape(String value) {
		if (value == null)
			return null;
		else {
			return value
					.replace("\\", "\\\\")
					// .replace("/", "\\/")
					.replace("\b", "\\b").replace("\f", "\\f")
					.replace("\n", "\\n").replace("\r", "\\r")
					.replace("\t", "\\t").replace("\"", "\\\"")
					.replace("\u0016", "").replace("\u0019", "").replace("\u0002", "");
		}

		/*
		 * 
		 * %x22 / ; " quotation mark U+0022 %x5C / ; \ reverse solidus U+005C
		 * %x2F / ; / solidus U+002F %x62 / ; b backspace U+0008 %x66 / ; f form
		 * feed U+000C %x6E / ; n line feed U+000A %x72 / ; r carriage return
		 * U+000D %x74 / ; t tab U+0009 %x75 4HEXDIG ) ; uXXXX U+XXXX
		 */
	}
	public static String jsonFormat(List values) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		String d="";
		if (values!=null)
		for (Object value : values)
			if (value != null) {
				b.append(d);
				b.append(jsonQuote(jsonEscape(value.toString())));
				d=",";
			}
		b.append("]");
		return b.toString();
	}

	/**
	 * Returns null, if the value is null, or a quoted string otherwise
	 * 
	 * @param value
	 * @return
	 */
	public static String jsonQuote(String value) {
		if (value == null)
			return null;
		else
			return String.format("\"%s\"", value);
	}

	/**
	 * Used for JSONP callbacks TODO: full name validation
	 * 
	 * @param functionName
	 * @return
	 */
	public static String jsonSanitizeCallback(String functionName) {
		if (functionName == null)
			return null;
		else
			return functionName.replace("(", "").replace(")", "");
	}

	public static String jsonExpNumber(Double value) {
		if (value == null || value.isNaN())
			return null;
		else
			return df.get().format(value);
	}

	public static String jsonNumber(Number value) {
		if (value == null)
			return null;
		else
			return nf.get().format(value);
	}

	public static String jsonNumber(Double value) {
		if (value == null || value.isNaN())
			return null;
		else if (Math.abs(value) < 1E-3)
			return df.get().format(value);
		else
			return nf.get().format(value);
	}

	public static String jsonNumber(Integer value) {
		if (value == null)
			return null;
		else
			return nf.get().format(value);
	}
	
	public static String jsonDate(Date value) {
		if (value == null)
			return null;
		else
			return datef.get().format(value);
	}

}
