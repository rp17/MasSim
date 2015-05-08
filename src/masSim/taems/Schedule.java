package masSim.taems;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
//import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;

import raven.Main;

public class Schedule {
	private boolean debugFlag = false;
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
	public synchronized void Merge(Schedule sch, ConcurrentHashMap<String,String> completedMethods)
	{
		Main.Message(debugFlag, "Old Schedule " + this.hashCode() + " : " + this.toString());
		Main.Message(debugFlag, "Merge Candidate " + sch.hashCode() + " : " + sch.toString());
		ScheduleElement first = null;
		ScheduleElement last = null;
		Queue<ScheduleElement> mergedList = new ConcurrentLinkedQueue<ScheduleElement>();
		List<ScheduleElement> oldElementsList = new ArrayList<ScheduleElement>();
		List<ScheduleElement> cachedNewList = new ArrayList<ScheduleElement>();
		List<ScheduleElement> removedList = new ArrayList<ScheduleElement>();
		//First, loop through all elements of new schedule
		for(ScheduleElement el : sch.items)
		{
			//Save its first and last methods, as these will be given priority over first and last methods of
			//old schedule
			if (el.getMethod().isStartMethod())
			{
				first = el;
			}
			else if (el.getMethod().isEndMethod())
			{
				last = el;
			}
			else
			{
				//Cache these new elements
				if (!completedMethods.containsKey(el.getMethod().getLabel()))
					cachedNewList.add(el);
			}
			//Remove all those elements from the old schedule, which are also present in new one 
			//since they'd be the updated versions of those same methods
			if (ContainsSameMethod(this.items,el))
			{
				//this.items.remove(el);Remove is not guaranteed to work in concurrent queue, so using custom mechanism
				removedList.add(el);
			}
		}
		//Now we have a pruned over schedule containing only those elements which were not present in new
		//one and thus need to be brought in. Loop through old schedule and bring them in
		for(ScheduleElement el : this.items)
		{
			if (!ContainsSameMethod(removedList,el))
			{
				if (el.getMethod().isStartMethod())//If first was not found in new schedule
				{
					if (first==null)
						first = el;
					//By this time we should have a first method, so add it to new merged schedule now
					mergedList.add(first);
				}
				else if (el.getMethod().isEndMethod())//If last was not found in new schedule
				{
					if (last==null)
						last = el;//By this time, we should have a last, but don't add it yet to schedule
				}
				else
				{
					if (!completedMethods.containsKey(el.getMethod().getLabel()))
						oldElementsList.add(el);//Add all elements of old schedule
				}
			}
		}
		//If old schedule was empty, first did not get added, so check and add now
		if (!ContainsSameMethod(mergedList,first))
		{
			mergedList.add(first);
		}
		//Now add old elements core items
		for(ScheduleElement el : oldElementsList)
		{
			mergedList.add(el);
		}
		//Now that we are done adding all elements of old schedule, along with a first, add new elements and last
		for(ScheduleElement el : cachedNewList)
		{
			mergedList.add(el);
		}
		//Finally add the last method
		mergedList.add(last);
		this.items = mergedList;
		Main.Message(false, "New Schedule " + this.hashCode() + " : " + this.toString());
	}
	
	private boolean ContainsSameMethod(Collection<ScheduleElement> one, ScheduleElement two)
	{
		//Different from equals() because it will check name rather than id
		for(ScheduleElement el : one)
		{
			//if (el.getName().equals(two.getName()))
			if (el.getName().equals(two.getName()))
			{
				return true;
			}
		}
		return false;
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
