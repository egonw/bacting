/* Copyright (c) 2019  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.managers;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.StringMatrix;

public class ExcelManager {

	private String workspaceRoot;

	public ExcelManager(String workspaceRoot) {
		this.workspaceRoot = workspaceRoot;
	}

	public StringMatrix getSheet(String filename, int sheetNumber) throws BioclipseException {
		return getSheet(filename, sheetNumber, false);
	}

	public StringMatrix getSheet(String filename, int sheetNumber, boolean hasHeader) throws BioclipseException {
        Workbook workbook;
		try {
			FileInputStream excelFile = new FileInputStream(
				Paths.get(workspaceRoot + filename).toFile()
			);
			workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(sheetNumber);
			Iterator<Row> iterator = datatypeSheet.iterator();

			StringMatrix results = new StringMatrix();
			int row = 0, col = 0;
			while (iterator.hasNext()) {
				row++;
				Row currentRow = iterator.next();
				Iterator<Cell> cellIterator = currentRow.iterator();
				col = 0;
				while (cellIterator.hasNext()) {
					col++;
					Cell currentCell = cellIterator.next();
					if (hasHeader && row == 1) {
						results.setColumnName(col, currentCell.getStringCellValue());
					} else {
						results.set(row, col, currentCell.getStringCellValue());
					}
				}
			}
			workbook.close();
			return results;
		} catch (IOException exception) {
			throw new BioclipseException("Could not open file: " + exception.getMessage(), exception);
		}
	}
}
