package com.sling.tv.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import javax.jcr.Session;
import javax.jcr.Node;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Sling Imports
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;

//This is a component so it can provide or consume services
@SlingServlet(paths = "/bin/customexcelfile", methods = "POST", metatype = true)
public class GameImporter extends SlingAllMethodsServlet {
	private static final long serialVersionUID = 2598426539166789515L;

	// Inject a Sling ResourceResolverFactory
	@Reference
	private ResourceResolverFactory resolverFactory;

	private Session session;

	/** Default log. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServerException, IOException {

	}

	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServerException, IOException {

		try {
			final boolean isMultipart = org.apache.commons.fileupload.servlet.ServletFileUpload
					.isMultipartContent(request);
			PrintWriter out = response.getWriter();
			
			if (isMultipart) {
				final java.util.Map<String,RequestParameter[]> params = request.getRequestParameterMap();
				for (final java.util.Map.Entry<String,RequestParameter[]> pairs : params.entrySet()) {
					
					final RequestParameter[] pArr = pairs.getValue();
					final RequestParameter param = pArr[0];
					final InputStream stream = param.getInputStream();

					// Save the uploaded file into the Adobe CQ DAM
					int excelValue = injectSpreadSheet(stream);
					if (excelValue == 0)
						out.println("Game data from the Excel Spread Sheet has been successfully imported into the AEM JCR");
					else
						out.println("Game data could not be imported into the AEM JCR");
				}
			}
		}

		catch (Exception e) {
			log.error("Error in Gamefinder import" + e.getMessage());
		}

	}

	// Get data from the excel spreadsheet
	@SuppressWarnings("deprecation")
	public int injectSpreadSheet(InputStream is) {
		try {

			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = sheet.rowIterator();
			int countRow = 0;
			
			while (iterator.hasNext()) {
				countRow++;
				Row currentRow = iterator.next();
				if(countRow==1) continue;
				Iterator<Cell> cellIterator = currentRow.cellIterator();
				Map<String,String> cellValues = new HashMap<String,String>();
				int countCell =0;

				while (cellIterator.hasNext()) {
					countCell++;
					Cell currentCell = cellIterator.next();
					String cellValue = "";
					if (currentCell.getCellTypeEnum() == CellType.STRING) {
						cellValue =  currentCell.getStringCellValue();
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                    	cellValue = Double.toString(currentCell.getNumericCellValue());
                    }
							
					cellValues.put(Integer.toString(countCell),cellValue);
				}

				int status = injestCustData(cellValues,Integer.toString(countRow));
				if(status==-1) {
					return -1;
				}
			}
			
			workbook.close();
			return 0;

		} catch (Exception e) {
			log.error("Error in Injecting" + e.getMessage());
			return -1;
		}
		
	}

	// Stores game data in the Adobe CQ JCR
	@SuppressWarnings("deprecation")
	public int injestCustData(Map<String,String> cellValues, String rowCell) {
		try {

			// Invoke the adaptTo method to create a Session used to create a
			// QueryManager
			ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
			session = resourceResolver.adaptTo(Session.class);

			String datetime = cellValues.get("1");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = (Date)formatter.parse(datetime); 
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			Node yearNode;
			Node monthNode;
			Node dayNode;
			

			// Create a node that represents the root node
			Node root = session.getRootNode();

			// Get the content node in the JCR
			Node sling_tv_node = root.getNode("content/sling-tv");
			Node gameRootNode = null;

			if (sling_tv_node.hasNode("game-data")) {
				gameRootNode = sling_tv_node.getNode("game-data");
			}else {
				gameRootNode = sling_tv_node.addNode("game-data","sling:Folder");
			}
			
			if(gameRootNode.hasNode(Integer.toString(year))) {
				yearNode = gameRootNode.getNode(Integer.toString(year));
			}else {
				yearNode = gameRootNode.addNode(Integer.toString(year),"sling:Folder");
			}
			
			if(yearNode.hasNode(Integer.toString(month))) {
				monthNode = yearNode.getNode(Integer.toString(month));
			}else {
				monthNode = yearNode.addNode(Integer.toString(month),"sling:Folder");
			}
			
			if(monthNode.hasNode(Integer.toString(day))) {
				dayNode = monthNode.getNode(Integer.toString(day));
			}else {
				dayNode = monthNode.addNode(Integer.toString(day),"sling:Folder");
			}
			
			Node gameNode;
			if(dayNode.hasNode("match" + rowCell)) {
				// Store content from the client JSP in the JCR
				  gameNode = dayNode.getNode("match" + rowCell);
				  gameNode.remove();
				  dayNode.save();
			}
			
			gameNode = dayNode.addNode("match" + rowCell, "nt:unstructured");
			

			// make sure name of node is unique
			gameNode.setProperty("affiliation", (String)cellValues.get("2"));
			gameNode.setProperty("channel_link", (String)cellValues.get("3"));
			gameNode.setProperty("network", (String)cellValues.get("3"));
			gameNode.setProperty("channel", (String)cellValues.get("4"));
			gameNode.setProperty("zipcodes", (String)cellValues.get("5"));
			gameNode.setProperty("display_data_desktop", (String)cellValues.get("6"));
			gameNode.setProperty("display_data_mobile", (String)cellValues.get("7"));
			gameNode.setProperty("search_data", (String)cellValues.get("8"));
			gameNode.setProperty("location", (String)cellValues.get("9"));
			gameNode.setProperty("team1", (String)cellValues.get("10"));
			gameNode.setProperty("team2", (String)cellValues.get("11"));
			gameNode.setProperty("drawer_display_zip", (String)cellValues.get("12"));
			gameNode.setProperty("drawer_display_nozip", (String)cellValues.get("13"));
			gameNode.setProperty("All", (String)cellValues.get("14"));

			// Save the session changes and log out
			session.save();
			session.logout();
			return 0;
		}
		catch (Exception e) {
			log.error("Error in Gamefinder import" + e.getMessage());
			return -1;
		}
	}

}