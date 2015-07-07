package masSim.taems;

import java.util.Collection;
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
	public synchronized void Merge(Schedule sch)
	{

		Main.Message(true, "Old Schedule " + this.hashCode() + " : " + this.toString());
		Main.Message(true, "Merge Candidate " + sch.hashCode() + " : " + sch.toString());
		ScheduleElement first = null;
		ScheduleElement last = null;
		boolean useSch = false;

		//First, set first and last, and remove all elements from old schedule, which are also present in the new schedule
		for(ScheduleElement el : sch.items) {
			if(el.getMethod().isStartMethod()) {
				first = el;
				useSch = true;
			} else if(el.getMethod().isEndMethod()) {
				last = el;
				sch.items.remove(el);
			} else if (ContainsSameMethod(this.items,el)) {
				this.items.remove(el);
			}
		}

		//If first was not in sch, then set it
		if(first == null) {
			for(ScheduleElement el : this.items) {
				if(el.getMethod().isStartMethod()) { 
					first = el;
				}
			}
		}
		//If last was not in sch, then set it
		if(last == null) {
			for(ScheduleElement el : this.items) {
				if(el.getMethod().isEndMethod()) {
					last = el;
					this.items.remove(el);
				}
			}
		}

		//add items to updated schedule
		if(useSch) {
			for(ScheduleElement el : this.items) {
				sch.items.add(el);
			}
			sch.items.add(last);
			this.items = sch.items;
		} else {
			for(ScheduleElement el : sch.items) {
				this.items.add(el);
			}
			this.items.add(last);
		}
		Main.Message(true, "New Schedule " + this.hashCode() + " : " + this.toString());

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
