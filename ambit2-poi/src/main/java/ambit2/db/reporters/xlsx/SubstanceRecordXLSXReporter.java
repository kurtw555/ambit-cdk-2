package ambit2.db.reporters.xlsx;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import ambit2.base.data.Profile;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.Template;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.ReliabilityParams._r_flags;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.study.IStudyPrinter;
import ambit2.core.io.study.StudyFormatter;
import ambit2.db.substance.study.SubstanceStudyDetailsProcessor;
import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.p.DefaultAmbitProcessor;

public class SubstanceRecordXLSXReporter<Q extends IQueryRetrieval<IStructureRecord>>
		extends AbstractXLSXReporter<Q> implements IStudyPrinter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3258781396076442458L;
	protected int rowIndex = 0;
	protected int substanceIndex = 0;
	protected StudyFormatter formatter;
	protected CellStyle sectionStyle, hstyle, tableStyle;
	protected Font blackFont, blueFont, redFont;

	public SubstanceRecordXLSXReporter(String baseRef, boolean hssf,
			SubstanceEndpointsBundle[] bundles, String configResource) {
		this(baseRef, hssf, null, null, bundles, null);
		formatter = new StudyFormatter(configResource);

	}

	@Override
	public void printHeader(int record, int column, int order, String value) {
		Row row = sheet.getRow(rowIndex + record);
		if (row == null)
			row = sheet.createRow(rowIndex + record);
		Cell cell = row.createCell(column);
		cell.setCellStyle(tableStyle);
		cell.setCellType(CellType.STRING);
		cell.setCellValue(value);
	}

	@Override
	public void print(int record, int column, int order, String value,
			boolean isResult, _r_flags studyResultType) {
		// uuid
		if (order == StudyFormatter._MAX_COL || value == null)
			return;
		Row row = sheet.getRow(rowIndex + record);
		if (row == null)
			row = sheet.createRow(rowIndex + record);
		Cell cell = row.createCell(column);
		cell.setCellStyle(tableStyle);
		cell.setCellType(CellType.STRING);

		if (studyResultType == null)
			cell.setCellValue(value);
		else {
			Font font = redFont;
			switch (studyResultType) {
			case experimentalresult: {
				font = blueFont;
				break;
			}
			case other: {
				font = blackFont;
				break;
			}
			case unsupported: {
				font = blackFont;
				break;
			}
			case NOTSPECIFIED: {
				font = blackFont;
				break;
			}
			case nodata: {
				font = blackFont;
				break;
			}
			default: {
				font = redFont;

			}
			}
			cell.setCellValue(createRichTextString(value, font));
		}

	}

	public SubstanceRecordXLSXReporter(String baseRef, boolean hssf,
			Template template, Profile groupedProperties,
			SubstanceEndpointsBundle[] bundles, String urlPrefix) {
		super(baseRef, hssf, template, groupedProperties, bundles, urlPrefix,
				false);

		SubstanceStudyDetailsProcessor paReader = new SubstanceStudyDetailsProcessor();

		getProcessors().clear();
		getProcessors().add(paReader);
		getProcessors()
				.add(new DefaultAmbitProcessor<IStructureRecord, IStructureRecord>() {
					@Override
					public IStructureRecord process(IStructureRecord target)
							throws Exception {
						processItem(target);
						return target;
					};
				});

		hstyle = workbook.createCellStyle();
		hstyle.setWrapText(true);
		hstyle.setAlignment(HorizontalAlignment.LEFT);
		hstyle.setVerticalAlignment(VerticalAlignment.TOP);
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		hstyle.setFont(headerFont);

		sectionStyle = workbook.createCellStyle();
		sectionStyle.setWrapText(true);
		sectionStyle.setAlignment(HorizontalAlignment.LEFT);
		sectionStyle.setVerticalAlignment(VerticalAlignment.TOP);
		blackFont = workbook.createFont();
		blackFont.setItalic(true);
		blackFont.setColor(IndexedColors.BLACK.getIndex());
		sectionStyle.setFont(blackFont);

		tableStyle = workbook.createCellStyle();
		tableStyle.setWrapText(true);
		tableStyle.setAlignment(HorizontalAlignment.LEFT);
		tableStyle.setVerticalAlignment(VerticalAlignment.TOP);
		tableStyle.setBorderBottom(BorderStyle.THIN);
		tableStyle.setBorderLeft(BorderStyle.THIN);
		tableStyle.setBorderRight(BorderStyle.THIN);
		tableStyle.setBorderTop(BorderStyle.THIN);

		blueFont = workbook.createFont();
		blueFont.setColor(IndexedColors.BLUE.getIndex());

		redFont = workbook.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
	}

	@Override
	public void header(OutputStream output, Q query) {
		Row row = sheet.createRow(rowIndex);

		Cell cell = row.createCell(0);
		cell.setCellStyle(hstyle);
		cell.setCellType(CellType.STRING);
		cell.setCellValue("Annex 1 Experimental data");
		rowIndex++;
		/*
		 * row = sheet.createRow(rowIndex); cell = row.createCell(1);
		 * cell.setCellType(Cell.CELL_TYPE_STRING); cell.setCellValue(
		 * "These are the experimental data given in the assessment matrix, which should be displayed when clicking the hyperlink in the matrix."
		 * );
		 */
		rowIndex++;
	}

	@Override
	public Object processItem(IStructureRecord item) throws Exception {
		if (item instanceof SubstanceRecord) {
			SubstanceRecord record = (SubstanceRecord) item;
			Row row = sheet.createRow(rowIndex);
			Cell cell = row.createCell(0);
			cell.setCellStyle(hstyle);
			cell.setCellType(CellType.STRING);
			cell.setCellValue(String.format("Substance %d:",
					(substanceIndex + 1)));

			cell = row.createCell(1);
			cell.setCellStyle(hstyle);
			cell.setCellType(CellType.STRING);
			cell.setCellValue(record.getPublicName() == null ? record
					.getSubstanceName() : record.getPublicName());
			rowIndex++;

			if (record.getMeasurements() != null) {
				// sort by category
				Collections.sort(record.getMeasurements(),
						new Comparator<ProtocolApplication>() {
							@Override
							public int compare(ProtocolApplication o1,
									ProtocolApplication o2) {
								return ((Protocol) o1.getProtocol())
										.getCategory().compareTo(
												((Protocol) o2.getProtocol())
														.getCategory());
							}
						});
				String current_category = null;
				for (ProtocolApplication<Protocol, String, String, Object, String> pa : record
						.getMeasurements()) {

					String caption_category;
					Protocol._categories category = null;
					try {
						category = Protocol._categories.valueOf(pa
								.getProtocol().getCategory());
						caption_category = String.format("%s. %s",
								category.getNumber(), category.toString());
					} catch (Exception x) {
						caption_category = pa.getProtocol().getCategory();
					}

					if (current_category == null
							|| !current_category.equals(caption_category)) {
						rowIndex++;
						row = sheet.createRow(rowIndex);
						cell = row.createCell(0);
						cell.setCellStyle(sectionStyle);
						cell.setCellType(CellType.STRING);
						cell.setCellValue(caption_category);
						current_category = caption_category;
						rowIndex++;
						try {
							formatter.formatCategoryHeader(pa.getProtocol()
									.getCategory(), this);
						} catch (Exception x) {
							logger.log(Level.WARNING, x.getMessage(),x);
						}
						rowIndex++;
					}
					// uuid
					row = sheet.createRow(rowIndex);
					cell = row.createCell(0);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(pa.getDocumentUUID());

					rowIndex += formatter.format(pa, this);

					//rowIndex++;
				}

				rowIndex++;
			}
			substanceIndex++;
		}
		return item;
	}

	@Override
	public void footer(OutputStream output, Q query) {
		sheet.autoSizeColumn(0, true);
		/*
		 * sheet.autoSizeColumn(1, true); sheet.autoSizeColumn(2, true);
		 * sheet.autoSizeColumn(3, true); sheet.autoSizeColumn(4, true);
		 * sheet.autoSizeColumn(5, true);
		 */
		super.footer(output, query);
	}
}
