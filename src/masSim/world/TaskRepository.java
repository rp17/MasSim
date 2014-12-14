package masSim.world;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import masSim.taems.ExactlyOneQAF;
import masSim.taems.QAF;
import masSim.taems.SeqSumQAF;
import masSim.taems.SumAllQAF;
import masSim.taems.Task;

public class TaskRepository {
	
	public Task ReadTasks()
	{
		
		try {
			//Get the DOM Builder Factory
			DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(ClassLoader.getSystemResourceAsStream("xml/employee.xml"));
			Task tasks;
			NodeList nodeList = document.getDocumentElement().getChildNodes();
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				
				
				
				//We have encountered an <employee> tag.
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					
			NodeList childNodes = node.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
			  Node cNode = childNodes.item(j);
			  //Identifying the child tag of employee encountered.
			  if (cNode instanceof Element) {
			    Task t = ParseTask(cNode);
			      }
			    }
			  }
			}
		} 
		catch (IOException e) {}
		catch(ParserConfigurationException e){}
		catch(SAXException e){}
		return null;	
	}
	
	private Task ParseTask(Node node)
	{
		String taskId;
		String taskName;
		String qafStringValue;
		QAF qaf;
		taskId = node.getAttributes().getNamedItem("id").getNodeValue();
		taskName = node.getAttributes().getNamedItem("name").getNodeValue();
		qafStringValue = node.getAttributes().getNamedItem("qaf").getNodeValue()toString().toLowerCase();
		if (qafStringValue=="sumall")
		{
			qaf = new SumAllQAF();
		}
		else if (qafStringValue=="exactlyone")
		{
			qaf = new ExactlyOneQAF();
		} 
		else if (qafStringValue=="seqsum")
		{
			qaf = new SeqSumQAF();
		}
		return new Task(taskName, qaf, null);
	}
}
