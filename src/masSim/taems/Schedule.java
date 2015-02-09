package masSim.taems;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;

import raven.Main;

public class Schedule {
	private Queue<ScheduleElement> items;
	public int TotalQuality = 0;
	public Schedule() {
		items = new ConcurrentLinkedQueue<ScheduleElement>();
	}
	public void addItem(ScheduleElement item){
		//Main.Message("[Schedule] Added to schedule task " + item.getName());
		items.add(item);
	}
	public void RemoveElement(ScheduleElement item)
	{
		items.remove(item);
	}
	public ScheduleElement poll(){
		return items.poll();
	}
	public ScheduleElement peek(){
		return items.peek();
	}
	public boolean hasNext(int ind) {
		return ind < items.size();
	}
	public Iterator<ScheduleElement> getItems() {
		return items.iterator();
	}
	public void Merge(Schedule sch)
	{
		Schedule mergedSchedule = new Schedule();
		ScheduleElement first = null;
		ScheduleElement last = null;
		PriorityQueue<ScheduleElement> orderedElements = new PriorityQueue<ScheduleElement>();
		for(ScheduleElement el : sch.items)
		{
			if (el.getMethod().isStartMethod())
			{
				first = el;
			}
			else if (el.getMethod().isEndMethod())
			{
				last = el;
			}
			else if (orderedElements.contains(el))
			{
				//skip
			}
			else
			{
				orderedElements.add(el);
			}
		}
		for(ScheduleElement el : this.items)
		{
			if (el.getMethod().isStartMethod())
			{
				first = el;
			}
			else if (el.getMethod().isEndMethod())
			{
				last = el;
			}
			else if (orderedElements.contains(el))
			{
				//skip
			}
			else
			{
				orderedElements.add(el);
			}
		}
		if (first != null)
			mergedSchedule.items.add(first);
		while(!orderedElements.isEmpty())
		{
			mergedSchedule.addItem(orderedElements.poll());
		}
		if (last != null)
			mergedSchedule.items.add(last);
		this.items = mergedSchedule.items;
	}
	@Override
	public String toString()
	{
		int quality = 0;
		String val = "";
		Iterator<ScheduleElement> it = items.iterator();
		while(it.hasNext())
		{
			ScheduleElement el = (ScheduleElement)it.next();
			val += " > " + el.toString();
			quality += el.getMethod().getOutcome().quality;
		}
		val += " | Quality = " + quality;
		return val;
	}
}
