package masSim.taems;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;

public class Schedule {
	private Queue<ScheduleElement> items;
	public Schedule() {
		items = new ConcurrentLinkedQueue<ScheduleElement>();
	}
	public void addItem(ScheduleElement item){
		items.add(item);
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
}
