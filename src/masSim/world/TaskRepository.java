package masSim.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import raven.Main;
import masSim.taems.ExactlyOneQAF;
import masSim.taems.Method;
import masSim.taems.QAF;
import masSim.taems.SeqSumQAF;
import masSim.taems.SumAllQAF;
import masSim.taems.Task;

public class TaskRepository {
	
	boolean debugFlag = true;
	String repositoryFolderPath = "";
	Map<String,Task> taskDefinitions;
	
	public TaskRepository()
	{
		this("TaskRepository");
	}
			
	public TaskRepository(String repositoryFolderPath)
	{
		this.repositoryFolderPath = repositoryFolderPath;
		this.taskDefinitions= new HashMap<String,Task>();
	}
	
	public Task ReadTaskDescriptions(String fileName)
	{
		try {
			File file = new File(repositoryFolderPath + "\\" + fileName);
			Main.Message(false, file.getAbsolutePath());
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			//Get the DOM Builder Factory
			DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fis);
			Task tasks;
			NodeList nodeList = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				//We have encountered a <Taems> tag.
				Node node = nodeList.item(i);
				if (node instanceof Element) {
					Task t = ParseTask(node);
					this.taskDefinitions.put(t.getLabel(), t);
					Main.Message(this, debugFlag, "Task " + t.label + " added to repository");
				}
			}
		} 
		catch (IOException e) {}
		catch(ParserConfigurationException e){}
		catch(SAXException e){}
		return null;	
	}
	
	public Task GetTask(String name)
	{
		if (!this.taskDefinitions.containsKey(name))
		{
			Main.Message(true, "Possible Error: Task " + name + " not found in repository");
		}
		return this.taskDefinitions.get(name);
	}
	
	private Task ParseTask(Node node)
	{
		String taskId;
		String taskName;
		String qafStringValue;
		QAF qaf = null;
		boolean recurring = false;
		boolean isTask = node.getNodeName()=="Task";
		taskId = node.getAttributes().getNamedItem("id").getNodeValue();
		taskName = node.getAttributes().getNamedItem("name").getNodeValue();
		if (isTask) {
			Node recurringNode = node.getAttributes().getNamedItem("recurring");
			if (recurringNode!=null)
				recurring = recurringNode.getNodeValue().toString().toLowerCase().equalsIgnoreCase("true");
			qafStringValue = node.getAttributes().getNamedItem("qaf").getNodeValue().toString().toLowerCase();
			if (qafStringValue.equalsIgnoreCase("sumall"))
			{
				qaf = new SumAllQAF();
			}
			else if (qafStringValue.equalsIgnoreCase("exactlyone"))
			{
				qaf = new ExactlyOneQAF();
			} 
			else if (qafStringValue.equalsIgnoreCase("seqsum"))
			{
				qaf = new SeqSumQAF();
			}
		}
		Task task = new Task(taskName, qaf, null, recurring);
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);
			if (childNode.getNodeName().equalsIgnoreCase("Method"))
			{
				Method childMethod = ParseMethod(childNode);
				task.addTask(childMethod);
			}
			else if (childNode.getNodeName().equalsIgnoreCase("Task"))
			{
				Task childTask = ParseTask(childNode);
				task.addTask(childTask);
			}
		}
		return task;
	}
	
	private Method ParseMethod(Node node)
	{
		String methodName = node.getAttributes().getNamedItem("name").getNodeValue();
		int quality = Integer.parseInt(node.getAttributes().getNamedItem("Quality").getNodeValue());
		int duration = Integer.parseInt(node.getAttributes().getNamedItem("Duration").getNodeValue());
		int xCoord = Integer.parseInt(node.getAttributes().getNamedItem("XCoord").getNodeValue());
		int yCoord = Integer.parseInt(node.getAttributes().getNamedItem("YCoord").getNodeValue());
		Method method = new Method(methodName,quality,duration,xCoord,yCoord,0,null);
		return method;
	}
}
