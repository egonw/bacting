/* Copyright (c) 2015,2020  Egon Willighagen <egon.willighagen@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.managers.report.serializer;

import java.util.List;

import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.report.data.Box;
import net.bioclipse.report.data.Header;
import net.bioclipse.report.data.Hyperlink;
import net.bioclipse.report.data.IReport;
import net.bioclipse.report.data.IReportContent;
import net.bioclipse.report.data.IndentEnd;
import net.bioclipse.report.data.IndentStart;
import net.bioclipse.report.data.NewLine;
import net.bioclipse.report.data.ParagraphEnd;
import net.bioclipse.report.data.ParagraphStart;
import net.bioclipse.report.data.Section;
import net.bioclipse.report.data.Table;
import net.bioclipse.report.data.Text;
import net.bioclipse.report.serializer.ISerializer;

/**
 * Serializes the report into Markdown. Reuses part of the Bioclipse
 * HTMLSerializer}.
 *
 * @author egonw
 */
public class MarkdownSerializer implements ISerializer {

	public String serialize(IReport report) {
		StringBuffer markdown = new StringBuffer();
		for (IReportContent content : report.getContent()) {
			if (content instanceof Header) {
				// not supported
			} else if (content instanceof Text) {
				Text text = (Text)content;
				List<Text.Style> styles = text.getStyles();
				if (styles.contains(Text.Style.ITALIC)) markdown.append("_");
				if (styles.contains(Text.Style.BOLD)) markdown.append("**");
				for (String line : text.getContent()) {
					markdown.append(line);
				}
				if (styles.contains(Text.Style.BOLD)) markdown.append("**");
				if (styles.contains(Text.Style.ITALIC)) markdown.append("_");
			} else if (content instanceof ParagraphStart) {
				markdown.append("<p>");
			} else if (content instanceof ParagraphEnd) {
				markdown.append("</p>\n");
			} else if (content instanceof IndentStart) {
				markdown.append("<ul>\n");
			} else if (content instanceof IndentEnd) {
				markdown.append("</ul>\n");
			} else if (content instanceof NewLine) {
				markdown.append("<br />\n");
			} else if (content instanceof Box) {
				Integer[] dims = (Integer[])((Box)content).getContent();
				markdown.append("<div style=\"width:").append(dims[1].intValue())
				  .append("px;height:").append(dims[0].intValue())
				  .append("px;border:1px solid #000; display: inline-block\" ></div>");
			} else if (content instanceof Section) {
				Section section = (Section)content;
				String title = section.getContent()[0];
				String level = section.getContent()[1];
				if ("level1".equals(level)) {
					markdown.append("\n# ").append(title).append("\n");
				} else if ("level2".equals(level)) {
					markdown.append("\n## ").append(title).append("\n");
				} else if ("level3".equals(level)) {
					markdown.append("\n### ").append(title).append("\n");
				}
			} else if (content instanceof Hyperlink) {
				Hyperlink link = (Hyperlink)content;
				markdown.append("[")
				    .append(link.getContent()[1])
	 			    .append("](")
	 			    .append(link.getContent()[0]).append(")");
            } else if (content instanceof Table) {
                Table table = (Table)content;
                IStringMatrix matrix = (IStringMatrix)table.getContent()[0];
                String caption = (String)table.getContent()[1];
                if (caption != null) markdown.append("<b>").append(caption).append("</b><br />\n");
                if (matrix != null & matrix.getRowCount() != 0) {
                    markdown.append("<table>\n");
                    // column headers
                    List<String> colNames = matrix.getColumnNames();
                    for (String colName : colNames) {
                        markdown.append("<td><b>").append(colName).append("</b></td>\n");
                    }
                    // table content
                    for (int i=1; i<=matrix.getRowCount(); i++) {
                        markdown.append("<tr>");
                        for (int j=1; j<=matrix.getColumnCount(); j++) {
                            markdown.append("<td>").append(
                                matrix.get(i, j)
                            ).append("</td>\n");
                        }
                        markdown.append("</tr>\n");
                    }
                    markdown.append("</table>\n");
                }
			}
		}
		return markdown.toString();
	}

}
