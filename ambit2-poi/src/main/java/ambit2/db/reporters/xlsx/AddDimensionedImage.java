package ambit2.db.reporters.xlsx;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

public class AddDimensionedImage {

	// Four constants that determine how - and indeed whether - the rows
	// and columns an image may overlie should be expanded to accomodate
	// that
	// image.
	// Passing EXPAND_ROW will result in the height of a row being increased
	// to accomodate the image if it is not already larger. The image will
	// be layed across one or more columns.
	// Passing EXPAND_COLUMN will result in the width of the column being
	// increased to accomodate the image if it is not already larger. The
	// image
	// will be layed across one or many rows.
	// Passing EXPAND_ROW_AND_COLUMN will result in the height of the row
	// bing increased along with the width of the column to accomdate the
	// image if either is not already larger.
	// Passing OVERLAY_ROW_AND_COLUMN will result in the image being layed
	// over one or more rows and columns. No row or column will be resized,
	// instead, code will determine how many rows and columns the image
	// should
	// overlie.
	public static final int EXPAND_ROW = 1;
	public static final int EXPAND_COLUMN = 2;
	public static final int EXPAND_ROW_AND_COLUMN = 3;
	public static final int OVERLAY_ROW_AND_COLUMN = 7;

	// Modified to support EMU - English Metric Units - used within the
	// OOXML
	// workbooks, this multoplier is used to convert between measurements in
	// millimetres and in EMUs
	private static final int EMU_PER_MM = 36000;

	/**
	 * Add an image to a worksheet.
	 * 
	 * @param cellNumber
	 *            A String that contains the location of the cell whose top left
	 *            hand corner should be aligned with the top left hand corner of
	 *            the image; for example "A1", "A2" etc. This is to support the
	 *            familiar Excel syntax. Whilst images are are not actually
	 *            inserted into cells this provides a convenient method of
	 *            indicating where the image should be positioned on the sheet.
	 * @param sheet
	 *            A reference to the sheet that contains the cell referenced
	 *            above.
	 * @param drawing
	 *            An instance of the DrawingPatriarch class. This is now passed
	 *            into the method where it was, previously, recovered from the
	 *            sheet in order to allow multiple pictures be inserted. If the
	 *            patriarch was not 'cached in this manner each time it was
	 *            created any previously positioned images would be simply
	 *            over-written.
	 * @param imageFile
	 *            An instance of the URL class that encapsulates the name of and
	 *            path to the image that is to be 'inserted into' the sheet.
	 * @param reqImageWidthMM
	 *            A primitive double that contains the required width of the
	 *            image in millimetres.
	 * @param reqImageHeightMM
	 *            A primitive double that contains the required height of the
	 *            image in millimetres.
	 * @param resizeBehaviour
	 *            A primitive int whose value will determine how the code should
	 *            react if the image is larger than the cell referenced by the
	 *            cellNumber parameter. Four constants are provided to determine
	 *            what should happen; AddDimensionedImage.EXPAND_ROW
	 *            AddDimensionedImage.EXPAND_COLUMN
	 *            AddDimensionedImage.EXPAND_ROW_AND_COLUMN
	 *            AddDimensionedImage.OVERLAY_ROW_AND_COLUMN
	 * @throws java.io.FileNotFoundException
	 *             If the file containing the image cannot be located.
	 * @throws java.io.IOException
	 *             If a problem occurs whilst reading the file of image data.
	 * @throws java.lang.IllegalArgumentException
	 *             If an invalid value is passed to the resizeBehaviour
	 *             parameter.
	 */
	public void addImageToSheet(String cellNumber, Sheet sheet, Drawing drawing, byte[] imageBytes, int imageType,
			int reqImageWidthPixel, int reqImageHeightPixel, int resizeBehaviour)
			throws IOException, IllegalArgumentException {
		// Convert the String into column and row indices then chain the
		// call to the overridden addImageToSheet() method.
		CellReference cellRef = new CellReference(cellNumber);
		this.addImageToSheet(cellRef.getCol(), cellRef.getRow(), sheet, drawing, imageBytes, imageType,
				reqImageWidthPixel, reqImageHeightPixel, resizeBehaviour);
	}

	/**
	 * Add an image to a worksheet.
	 * 
	 * @param colNumber
	 *            A primitive int that contains the index number of a column on
	 *            the worksheet; POI column indices are zero based. Together
	 *            with the rowNumber parameter's value, this parameter
	 *            identifies a cell on the worksheet. The images top left hand
	 *            corner will be aligned with the top left hand corner of this
	 *            cell.
	 * @param rowNumber
	 *            A primitive int that contains the index number of a row on the
	 *            worksheet; POI row indices are zero based. Together with the
	 *            rowNumber parameter's value, this parameter identifies a cell
	 *            on the worksheet. The images top left hand corner will be
	 *            aligned with the top left hand corner of this cell.
	 * @param sheet
	 *            A reference to the sheet that contains the cell identified by
	 *            the two parameters above.
	 * @param drawing
	 *            An instance of the DrawingPatriarch class. This is now passed
	 *            into the method where it was, previously, recovered from the
	 *            sheet in order to allow multiple pictures be inserted. If the
	 *            patriarch was not 'cached in this manner each time it was
	 *            created any previously positioned images would be simply
	 *            over-written.
	 * @param imageFile
	 *            An instance of the URL class that encapsulates the name of and
	 *            path to the image that is to be 'inserted into' the sheet.
	 * @param reqImageWidthMM
	 *            A primitive double that contains the required width of the
	 *            image in millimetres.
	 * @param reqImageHeightMM
	 *            A primitive double that contains the required height of the
	 *            image in millimetres.
	 * @param resizeBehaviour
	 *            A primitive int whose value will determine how the code should
	 *            react if the image is larger than the cell referenced by the
	 *            colNumber and rowNumber parameters. Four constants are
	 *            provided to determine what should happen;
	 *            AddDimensionedImage.EXPAND_ROW
	 *            AddDimensionedImage.EXPAND_COLUMN
	 *            AddDimensionedImage.EXPAND_ROW_AND_COLUMN
	 *            AddDimensionedImage.OVERLAY_ROW_AND_COLUMN
	 * @throws java.io.FileNotFoundException
	 *             If the file containing the image cannot be located.
	 * @throws java.io.IOException
	 *             If a problem occurs whilst reading the file of image data.
	 * @throws java.lang.IllegalArgumentException
	 *             If an invalid value is passed to the resizeBehaviour
	 *             parameter or if the extension of the image file indicates
	 *             that it is of a type that cannot currently be added to the
	 *             worksheet.
	 */
	public void addImageToSheet(int colNumber, int rowNumber, Sheet sheet, Drawing drawing, byte[] imageBytes,
			int imageType, int reqImageWidthPixel, int reqImageHeightPixel, int resizeBehaviour)
			throws IOException, IllegalArgumentException {
		ClientAnchor anchor = null;
		ClientAnchorDetail rowClientAnchorDetail = null;
		ClientAnchorDetail colClientAnchorDetail = null;

		// Validate the resizeBehaviour parameter.
		if ((resizeBehaviour != AddDimensionedImage.EXPAND_COLUMN)
				&& (resizeBehaviour != AddDimensionedImage.EXPAND_ROW)
				&& (resizeBehaviour != AddDimensionedImage.EXPAND_ROW_AND_COLUMN)
				&& (resizeBehaviour != AddDimensionedImage.OVERLAY_ROW_AND_COLUMN)) {
			throw new IllegalArgumentException("Invalid value passed to the "
					+ "resizeBehaviour parameter of AddDimensionedImage.addImageToSheet()");
		}

		// Call methods to calculate how the image and sheet should be
		// manipulated to accomodate the image; columns and then rows.

		double reqImageWidthMM = ConvertImageUnits
				.widthUnits2Millimetres(ConvertImageUnits.pixel2WidthUnits(reqImageWidthPixel));
		double reqImageHeightMM = ConvertImageUnits
				.widthUnits2Millimetres(ConvertImageUnits.pixel2WidthUnits(reqImageHeightPixel));

		colClientAnchorDetail = this.fitImageToColumns(sheet, colNumber, reqImageWidthMM, resizeBehaviour);
		rowClientAnchorDetail = this.fitImageToRows(sheet, rowNumber, reqImageHeightMM, resizeBehaviour);

		// Having determined if and how to resize the rows, columns and/or
		// the
		// image, create the ClientAnchor object to position the image on
		// the worksheet. Note how the two ClientAnchorDetail records are
		// interrogated to recover the row/column co-ordinates and any
		// insets.
		// The first two parameters are not used currently but could be if
		// the
		// need arose to extend the functionality of this code by adding the
		// ability to specify that a clear 'border' be placed around the
		// image.
		anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();

		anchor.setDx1(0);
		anchor.setDy1(0);
		anchor.setDx2(colClientAnchorDetail.getInset());
		anchor.setDy2(rowClientAnchorDetail.getInset());
		anchor.setCol1(colClientAnchorDetail.getFromIndex());
		anchor.setRow1(rowClientAnchorDetail.getFromIndex());
		anchor.setCol2(colClientAnchorDetail.getToIndex());
		anchor.setRow2(rowClientAnchorDetail.getToIndex());

		// For now, set the anchor type to do not move or resize the
		// image as the size of the row/column is adjusted. This could
		// easilly
		// become another parameter passed to the method. Please read the
		// note
		// above regarding the behaviour of image resizing.
		// anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
		anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);

		int index = sheet.getWorkbook().addPicture(imageBytes, imageType);
		drawing.createPicture(anchor, index);
	}

	/**
	 * Determines whether the sheets columns should be re-sized to accomodate
	 * the image, adjusts the columns width if necessary and creates then
	 * returns a ClientAnchorDetail object that facilitates construction of an
	 * ClientAnchor that will fix the image on the sheet and establish it's
	 * size.
	 * 
	 * @param sheet
	 *            A reference to the sheet that will 'contain' the image.
	 * @param colNumber
	 *            A primtive int that contains the index number of a column on
	 *            the sheet.
	 * @param reqImageWidthMM
	 *            A primitive double that contains the required width of the
	 *            image in millimetres
	 * @param resizeBehaviour
	 *            A primitive int whose value will indicate how the width of the
	 *            column should be adjusted if the required width of the image
	 *            is greater than the width of the column.
	 * @return An instance of the ClientAnchorDetail class that will contain the
	 *         index number of the column containing the cell whose top left
	 *         hand corner also defines the top left hand corner of the image,
	 *         the index number column containing the cell whose top left hand
	 *         corner also defines the bottom right hand corner of the image and
	 *         an inset that determines how far the right hand edge of the image
	 *         can protrude into the next column - expressed as a specific
	 *         number of coordinate positions.
	 */
	private ClientAnchorDetail fitImageToColumns(Sheet sheet, int colNumber, double reqImageWidthMM,
			int resizeBehaviour) {

		double colWidthMM = 0.0D;
		double colCoordinatesPerMM = 0.0D;
		int pictureWidthCoordinates = 0;
		ClientAnchorDetail colClientAnchorDetail = null;

		// Get the colum's width in millimetres
		colWidthMM = ConvertImageUnits.widthUnits2Millimetres((short) sheet.getColumnWidth(colNumber));

		// Check that the column's width will accomodate the image at the
		// required dimension. If the width of the column is LESS than the
		// required width of the image, decide how the application should
		// respond - resize the column or overlay the image across one or
		// more
		// columns.
		if (colWidthMM < reqImageWidthMM) {

			// Should the column's width simply be expanded?
			if ((resizeBehaviour == AddDimensionedImage.EXPAND_COLUMN)
					|| (resizeBehaviour == AddDimensionedImage.EXPAND_ROW_AND_COLUMN)) {
				// Set the width of the column by converting the required
				// image
				// width from millimetres into Excel's column width units.
				sheet.setColumnWidth(colNumber, ConvertImageUnits.millimetres2WidthUnits(reqImageWidthMM));
				// To make the image occupy the full width of the column,
				// convert
				// the required width of the image into co-ordinates. This
				// value
				// will become the inset for the ClientAnchorDetail class
				// that
				// is then instantiated.
				if (sheet instanceof HSSFSheet) {
					colWidthMM = reqImageWidthMM;
					colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
					pictureWidthCoordinates = (int) (reqImageWidthMM * colCoordinatesPerMM);

				} else {
					pictureWidthCoordinates = (int) reqImageWidthMM * AddDimensionedImage.EMU_PER_MM;
				}
				colClientAnchorDetail = new ClientAnchorDetail(colNumber, colNumber, pictureWidthCoordinates);
			}
			// If the user has chosen to overlay both rows and columns or
			// just
			// to expand ONLY the size of the rows, then calculate how to
			// lay
			// the image out across one or more columns.
			else if ((resizeBehaviour == AddDimensionedImage.OVERLAY_ROW_AND_COLUMN)
					|| (resizeBehaviour == AddDimensionedImage.EXPAND_ROW)) {
				colClientAnchorDetail = this.calculateColumnLocation(sheet, colNumber, reqImageWidthMM);
			}
		}
		// If the column is wider than the image.
		else {
			if (sheet instanceof HSSFSheet) {
				// Mow many co-ordinate positions are there per millimetre?
				colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
				// Given the width of the image, what should be it's
				// co-ordinate?
				pictureWidthCoordinates = (int) (reqImageWidthMM * colCoordinatesPerMM);
			} else {
				pictureWidthCoordinates = (int) reqImageWidthMM * AddDimensionedImage.EMU_PER_MM;
			}
			colClientAnchorDetail = new ClientAnchorDetail(colNumber, colNumber, pictureWidthCoordinates);
		}
		return (colClientAnchorDetail);
	}

	/**
	 * Determines whether the sheets row should be re-sized to accomodate the
	 * image, adjusts the rows height if necessary and creates then returns a
	 * ClientAnchorDetail object that facilitates construction of a ClientAnchor
	 * that will fix the image on the sheet and establish it's size.
	 * 
	 * @param sheet
	 *            A reference to the sheet that will 'contain' the image.
	 * @param rowNumber
	 *            A primitive int that contains the index number of a row on the
	 *            sheet.
	 * @param reqImageHeightMM
	 *            A primitive double that contains the required height of the
	 *            image in millimetres
	 * @param resizeBehaviour
	 *            A primitive int whose value will indicate how the height of
	 *            the row should be adjusted if the required height of the image
	 *            is greater than the height of the row.
	 * @return An instance of the ClientAnchorDetail class that will contain the
	 *         index number of the row containing the cell whose top left hand
	 *         corner also defines the top left hand corner of the image, the
	 *         index number of the row containing the cell whose top left hand
	 *         corner also defines the bottom right hand corner of the image and
	 *         an inset that determines how far the bottom edge of the image can
	 *         protrude into the next (lower) row - expressed as a specific
	 *         number of coordinate positions.
	 */
	private ClientAnchorDetail fitImageToRows(Sheet sheet, int rowNumber, double reqImageHeightMM,
			int resizeBehaviour) {
		Row row = null;
		double rowHeightMM = 0.0D;
		double rowCoordinatesPerMM = 0.0D;
		int pictureHeightCoordinates = 0;
		ClientAnchorDetail rowClientAnchorDetail = null;

		// Get the row and it's height
		row = sheet.getRow(rowNumber);
		if (row == null) {
			// Create row if it does not exist.
			row = sheet.createRow(rowNumber);
		}

		// Get the row's height in millimetres
		rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;

		// Check that the row's height will accomodate the image at the
		// required
		// dimensions. If the height of the row is LESS than the required
		// height
		// of the image, decide how the application should respond - resize
		// the
		// row or overlay the image across a series of rows.
		if (rowHeightMM < reqImageHeightMM) {
			if ((resizeBehaviour == AddDimensionedImage.EXPAND_ROW)
					|| (resizeBehaviour == AddDimensionedImage.EXPAND_ROW_AND_COLUMN)) {
				row.setHeightInPoints((float) (reqImageHeightMM * ConvertImageUnits.POINTS_PER_MILLIMETRE));
				if (sheet instanceof HSSFSheet) {
					rowHeightMM = reqImageHeightMM;
					rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
					pictureHeightCoordinates = (int) (reqImageHeightMM * rowCoordinatesPerMM);
				} else {
					pictureHeightCoordinates = (int) (reqImageHeightMM * AddDimensionedImage.EMU_PER_MM);
				}
				rowClientAnchorDetail = new ClientAnchorDetail(rowNumber, rowNumber, pictureHeightCoordinates);
			}
			// If the user has chosen to overlay both rows and columns or
			// just
			// to expand ONLY the size of the columns, then calculate how to
			// lay
			// the image out ver one or more rows.
			else if ((resizeBehaviour == AddDimensionedImage.OVERLAY_ROW_AND_COLUMN)
					|| (resizeBehaviour == AddDimensionedImage.EXPAND_COLUMN)) {
				rowClientAnchorDetail = this.calculateRowLocation(sheet, rowNumber, reqImageHeightMM);
			}
		}
		// Else, if the image is smaller than the space available
		else {
			if (sheet instanceof HSSFSheet) {
				rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
				pictureHeightCoordinates = (int) (reqImageHeightMM * rowCoordinatesPerMM);
			} else {
				pictureHeightCoordinates = (int) (reqImageHeightMM * AddDimensionedImage.EMU_PER_MM);
			}
			rowClientAnchorDetail = new ClientAnchorDetail(rowNumber, rowNumber, pictureHeightCoordinates);
		}
		return (rowClientAnchorDetail);
	}

	/**
	 * If the image is to overlie more than one column, calculations need to be
	 * performed to determine how many columns and whether the image will
	 * overlie just a part of one column in order to be presented at the
	 * required size.
	 * 
	 * @param sheet
	 *            The sheet that will 'contain' the image.
	 * @param startingColumn
	 *            A primitive int whose value is the index of the column that
	 *            contains the cell whose top left hand corner should be aligned
	 *            with the top left hand corner of the image.
	 * @param reqImageWidthMM
	 *            A primitive double whose value will indicate the required
	 *            width of the image in millimetres.
	 * @return An instance of the ClientAnchorDetail class that will contain the
	 *         index number of the column containing the cell whose top left
	 *         hand corner also defines the top left hand corner of the image,
	 *         the index number column containing the cell whose top left hand
	 *         corner also defines the bottom right hand corner of the image and
	 *         an inset that determines how far the right hand edge of the image
	 *         can protrude into the next column - expressed as a specific
	 *         number of coordinate positions.
	 */
	private ClientAnchorDetail calculateColumnLocation(Sheet sheet, int startingColumn, double reqImageWidthMM) {
		ClientAnchorDetail anchorDetail = null;
		double totalWidthMM = 0.0D;
		double colWidthMM = 0.0D;
		double overlapMM = 0.0D;
		double coordinatePositionsPerMM = 0.0D;
		int toColumn = startingColumn;
		int inset = 0;

		// Calculate how many columns the image will have to
		// span in order to be presented at the required size.
		while (totalWidthMM < reqImageWidthMM) {
			colWidthMM = ConvertImageUnits.widthUnits2Millimetres((short) (sheet.getColumnWidth(toColumn)));
			// Note use of the cell border width constant. Testing with an
			// image
			// declared to fit exactly into one column demonstrated that
			// it's
			// width was greater than the width of the column the POI
			// returned.
			// Further, this difference was a constant value that I am
			// assuming
			// related to the cell's borders. Either way, that difference
			// needs
			// to be allowed for in this calculation.
			totalWidthMM += (colWidthMM + ConvertImageUnits.CELL_BORDER_WIDTH_MILLIMETRES);
			toColumn++;
		}
		// De-crement by one the last column value.
		toColumn--;
		// Highly unlikely that this will be true but, if the width of a
		// series
		// of columns is exactly equal to the required width of the image,
		// then
		// simply build a ClientAnchorDetail object with an inset equal to
		// the
		// total number of co-ordinate positions available in a column, a
		// from column co-ordinate (top left hand corner) equal to the value
		// of the startingColumn parameter and a to column co-ordinate equal
		// to the toColumn variable.
		//
		// Convert both values to ints to perform the test.
		if ((int) totalWidthMM == (int) reqImageWidthMM) {
			// A problem could occur if the image is sized to fit into one
			// or
			// more columns. If that occurs, the value in the toColumn
			// variable
			// will be in error. To overcome this, there are two options, to
			// ibcrement the toColumn variable's value by one or to pass the
			// total number of co-ordinate positions to the third paramater
			// of the ClientAnchorDetail constructor. For no sepcific
			// reason,
			// the latter option is used below.
			if (sheet instanceof HSSFSheet) {
				anchorDetail = new ClientAnchorDetail(startingColumn, toColumn,
						ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS);
			} else {
				anchorDetail = new ClientAnchorDetail(startingColumn, toColumn,
						(int) reqImageWidthMM * AddDimensionedImage.EMU_PER_MM);
			}
		}
		// In this case, the image will overlap part of another column and
		// it is
		// necessary to calculate just how much - this will become the inset
		// for the ClientAnchorDetail object.
		else {
			// Firstly, claculate how much of the image should overlap into
			// the next column.
			overlapMM = reqImageWidthMM - (totalWidthMM - colWidthMM);

			// When the required size is very close indded to the column
			// size,
			// the calcaulation above can produce a negative value. To
			// prevent
			// problems occuring in later caculations, this is simply
			// removed
			// be setting the overlapMM value to zero.
			if (overlapMM < 0) {
				overlapMM = 0.0D;
			}

			if (sheet instanceof HSSFSheet) {
				// Next, from the columns width, calculate how many
				// co-ordinate
				// positons there are per millimetre
				coordinatePositionsPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
				// From this figure, determine how many co-ordinat positions
				// to
				// inset the left hand or bottom edge of the image.
				inset = (int) (coordinatePositionsPerMM * overlapMM);
			} else {
				inset = (int) overlapMM * AddDimensionedImage.EMU_PER_MM;
			}

			// Now create the ClientAnchorDetail object, setting the from
			// and to
			// columns and the inset.
			anchorDetail = new ClientAnchorDetail(startingColumn, toColumn, inset);
		}
		return (anchorDetail);
	}

	/**
	 * If the image is to overlie more than one rows, calculations need to be
	 * performed to determine how many rows and whether the image will overlie
	 * just a part of one row in order to be presented at the required size.
	 * 
	 * @param sheet
	 *            The sheet that will 'contain' the image.
	 * @param startingRow
	 *            A primitive int whose value is the index of the row that
	 *            contains the cell whose top left hand corner should be aligned
	 *            with the top left hand corner of the image.
	 * @param reqImageHeightMM
	 *            A primitive double whose value will indicate the required
	 *            height of the image in millimetres.
	 * @return An instance of the ClientAnchorDetail class that will contain the
	 *         index number of the row containing the cell whose top left hand
	 *         corner also defines the top left hand corner of the image, the
	 *         index number of the row containing the cell whose top left hand
	 *         corner also defines the bottom right hand corner of the image and
	 *         an inset that determines how far the bottom edge can protrude
	 *         into the next (lower) row - expressed as a specific number of
	 *         co-ordinate positions.
	 */
	private ClientAnchorDetail calculateRowLocation(Sheet sheet, int startingRow, double reqImageHeightMM) {
		ClientAnchorDetail clientAnchorDetail = null;
		Row row = null;
		double rowHeightMM = 0.0D;
		double totalRowHeightMM = 0.0D;
		double overlapMM = 0.0D;
		double rowCoordinatesPerMM = 0.0D;
		int toRow = startingRow;
		int inset = 0;

		// Step through the rows in the sheet and accumulate a total of
		// their
		// heights.
		while (totalRowHeightMM < reqImageHeightMM) {
			row = sheet.getRow(toRow);
			// Note, if the row does not already exist on the sheet then
			// create
			// it here.
			if (row == null) {
				row = sheet.createRow(toRow);
			}
			// Get the row's height in millimetres and add to the running
			// total.
			rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;
			totalRowHeightMM += rowHeightMM;
			toRow++;
		}
		// Owing to the way the loop above works, the rowNumber will have
		// been
		// incremented one row too far. Undo that here.
		toRow--;
		// Check to see whether the image should occupy an exact number of
		// rows. If so, build the ClientAnchorDetail record to point
		// to those rows and with an inset of the total number of
		// co-ordinate
		// position in the row.
		//
		// To overcome problems that can occur with comparing double values
		// for
		// equality, cast both to int(s) to truncate the value; VERY crude
		// and
		// I do not really like it!!
		if ((int) totalRowHeightMM == (int) reqImageHeightMM) {
			if (sheet instanceof HSSFSheet) {
				clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow,
						ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS);
			} else {
				clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow,
						(int) reqImageHeightMM * AddDimensionedImage.EMU_PER_MM);
			}
		} else {
			// Calculate how far the image will project into the next row.
			// Note
			// that the height of the last row assessed is subtracted from
			// the
			// total height of all rows assessed so far.
			overlapMM = reqImageHeightMM - (totalRowHeightMM - rowHeightMM);

			// To prevent an exception being thrown when the required width
			// of
			// the image is very close indeed to the column size.
			if (overlapMM < 0) {
				overlapMM = 0.0D;
			}

			if (sheet instanceof HSSFSheet) {
				rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
				inset = (int) (overlapMM * rowCoordinatesPerMM);
			} else {
				inset = (int) overlapMM * AddDimensionedImage.EMU_PER_MM;
			}
			clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow, inset);
		}
		return (clientAnchorDetail);
	}

	/**
	 * The HSSFClientAnchor class accepts eight arguments. In order, these are;
	 * 
	 * * How far the left hand edge of the image is inset from the left hand
	 * edge of the cell * How far the top edge of the image is inset from the
	 * top of the cell * How far the right hand edge of the image is inset from
	 * the left hand edge of the cell * How far the bottom edge of the image is
	 * inset from the top of the cell. * Together, arguments five and six
	 * determine the column and row coordinates of the cell whose top left hand
	 * corner will be aligned with the images top left hand corner. * Together,
	 * arguments seven and eight determine the column and row coordinates of the
	 * cell whose top left hand corner will be aligned with the images bottom
	 * right hand corner.
	 * 
	 * An instance of the ClientAnchorDetail class provides three of the eight
	 * parameters, one of the coordinates for the images top left hand corner,
	 * one of the coordinates for the images bottom right hand corner and either
	 * how far the image should be inset from the top or the left hand edge of
	 * the cell.
	 * 
	 * @author Mark Beardsley [msb at apache.org]
	 * @version 1.00 5th August 2009.
	 */
	public class ClientAnchorDetail {

		public int fromIndex = 0;
		public int toIndex = 0;
		public int inset = 0;

		/**
		 * Create a new instance of the ClientAnchorDetail class using the
		 * following parameters.
		 * 
		 * @param fromIndex
		 *            A primitive int that contains one of the coordinates (row
		 *            or column index) for the top left hand corner of the
		 *            image.
		 * @param toIndex
		 *            A primitive int that contains one of the coordinates (row
		 *            or column index) for the bottom right hand corner of the
		 *            image.
		 * @param inset
		 *            A primitive int that contains a value which indicates how
		 *            far the image should be inset from the top or the left
		 *            hand edge of a cell.
		 */
		public ClientAnchorDetail(int fromIndex, int toIndex, int inset) {
			this.fromIndex = fromIndex;
			this.toIndex = toIndex;
			this.inset = inset;
		}

		/**
		 * Get one of the number of the column or row that contains the cell
		 * whose top left hand corner will be aligned with the top left hand
		 * corner of the image.
		 * 
		 * @return The value - row or column index - for one of the coordinates
		 *         of the top left hand corner of the image.
		 */
		public int getFromIndex() {
			return (this.fromIndex);
		}

		/**
		 * Get one of the number of the column or row that contains the cell
		 * whose top left hand corner will be aligned with the bottom right hand
		 * corner of the image.
		 * 
		 * @return The value - row or column index - for one of the coordinates
		 *         of the bottom right hand corner of the image.
		 */
		public int getToIndex() {
			return (this.toIndex);
		}

		/**
		 * Get the images offset from the edge of a cell.
		 * 
		 * @return How far either the right hand or bottom edge of the image is
		 *         inset from the left hand or top edge of a cell.
		 */
		public int getInset() {
			return (this.inset);
		}
	}

	/**
	 * Utility methods used to convert Excels character based column and row
	 * size measurements into pixels and/or millimetres. The class also contains
	 * various constants that are required in other calculations.
	 * 
	 * @author xio[darjino@hotmail.com]
	 * @version 1.01 30th July 2009. Added by Mark Beardsley [msb at
	 *          apache.org]. Additional constants. widthUnits2Millimetres() and
	 *          millimetres2Units() methods.
	 */
	public static class ConvertImageUnits {

		// Each cell conatins a fixed number of co-ordinate points; this
		// number
		// does not vary with row height or column width or with font. These
		// two
		// constants are defined below.
		public static final int TOTAL_COLUMN_COORDINATE_POSITIONS = 1023; // MB
		public static final int TOTAL_ROW_COORDINATE_POSITIONS = 255; // MB
		// The resoultion of an image can be expressed as a specific number
		// of pixels per inch. Displays and printers differ but 96 pixels
		// per
		// inch is an acceptable standard to beging with.
		public static final int PIXELS_PER_INCH = 96; // MB
		// Cnstants that defines how many pixels and points there are in a
		// millimetre. These values are required for the conversion
		// algorithm.
		public static final double PIXELS_PER_MILLIMETRES = 3.78; // MB
		public static final double POINTS_PER_MILLIMETRE = 2.83; // MB
		// The column width returned by HSSF and the width of a picture when
		// positioned to exactly cover one cell are different by almost
		// exactly
		// 2mm - give or take rounding errors. This constant allows that
		// additional amount to be accounted for when calculating how many
		// celles the image ought to overlie.
		public static final double CELL_BORDER_WIDTH_MILLIMETRES = 2.0D; // MB
		public static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
		public static final int UNIT_OFFSET_LENGTH = 7;
		public static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109, 146, 182, 219 };

		/**
		 * pixel units to excel width units(units of 1/256th of a character
		 * width)
		 * 
		 * @param pxs
		 * @return
		 */
		public static short pixel2WidthUnits(int pxs) {
			short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH));
			widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];
			return widthUnits;
		}

		/**
		 * excel width units(units of 1/256th of a character width) to pixel
		 * units.
		 * 
		 * @param widthUnits
		 * @return
		 */
		public static int widthUnits2Pixel(short widthUnits) {
			int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR) * UNIT_OFFSET_LENGTH;
			int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
			pixels += Math.round(offsetWidthUnits / ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));
			return pixels;
		}

		/**
		 * Convert Excels width units into millimetres.
		 * 
		 * @param widthUnits
		 *            The width of the column or the height of the row in Excels
		 *            units.
		 * @return A primitive double that contains the columns width or rows
		 *         height in millimetres.
		 */
		public static double widthUnits2Millimetres(short widthUnits) {
			return (ConvertImageUnits.widthUnits2Pixel(widthUnits) / ConvertImageUnits.PIXELS_PER_MILLIMETRES);
		}

		/**
		 * Convert into millimetres Excels width units..
		 * 
		 * @param millimetres
		 *            A primitive double that contains the columns width or rows
		 *            height in millimetres.
		 * @return A primitive int that contains the columns width or rows
		 *         height in Excels units.
		 */
		public static int millimetres2WidthUnits(double millimetres) {
			return (ConvertImageUnits.pixel2WidthUnits((int) (millimetres * ConvertImageUnits.PIXELS_PER_MILLIMETRES)));
		}

		public static int pointsToPixels(double points) {
			return (int) Math.round(points / 72D * PIXELS_PER_INCH);
		}

		public static double pointsToMillimeters(double points) {
			return points / 72D * 25.4;
		}
	}
}
