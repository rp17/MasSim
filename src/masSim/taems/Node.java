package masSim.taems;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import masSim.world.MqttMessagingProvider;

public abstract class Node extends Element  implements IObserver {
	public static final int    EXECUTING = 1;
	public static final int    COMPLETED = 2;
	public static final int    ACTIVE = 3;
	protected int NodeType;
	protected boolean recurring = false;
	protected transient int status;
	protected CopyOnWriteArrayList<Node> children;
	public ArrayList<Node> Observers = new ArrayList<Node>();
	public abstract boolean IsTask();
	public Iterator<Node> getSubtasks(){
		return children.iterator();
	}
	
	//Proper traversal should be implemented
	
	public void Cleanup(MqttMessagingProvider mq){}

	public void Cleanup(){}
	
	  /**
	   * Default constructor
	   * @param l The node's label
	   * @param a The agent the node belongs to
	   */
	  public Node(String l, IAgent a) {

	    label = l;
	    agent = a;
	  }
	  
	  /**
	   * Default constructor
	   * @param l The node's label
	   */
	  public Node(String l) {
	    this (l, null);
	  }

	  /**
	   * Blank constructor
	   */
	  public Node() {
	    this(null, null);
	  }	
	  
	  /**
	   * Accessors
	   */
	  public String getLabel() { return label; }
	  public void setLabel(String l) { label = l;}
	  public IAgent getAgent() { return agent; }
	  public void setAgent(IAgent a) { agent = a; }
	  public boolean isVirtual() { return false; }
	  public int getStatus() { return status; }
	  
	  /**
	   * Determines if an object matches this one.  The matching
	   * details are as follows:
	   * <UL>
	   * <LI>[Reference]Null == anything
	   * <LI>[String]ABC == any string starting with ABC (case sensitive)
	   * <LI>[integer]Integer.MIN_VALUE == any integer
	   * <LI>[long]Long.MIN_VALUE == any long
	   * <LI>[double]Double.NEGATIVE_INFINITY == any double
	   * <LI>[float]Float.NEGATIVE_INFINITY == any float
	   * <LI>[boolean]Strict, no wildcards available for booleans
	   *  (** except if stored as Boolean, where null is wild)
	   * <LI>[Distribution]Null == anything
	   * </UL>
	   * This function will return true if all the fields match within
	   * this node.
	   * <!-- (e.g. it will not consider actual subtasks, supertasks or
	   * interrelationships). -->
	   * Interrelationships are not checked (you can do that explicitly
	   * yourself if this is important).  Matching in derived classes
	   * should work analagously (and should also check instance type).
	   * <BR>
	   * This matches against:
	   * <UL>
	   * <LI> Label
	   * <LI> Agent
	   * </UL>
	   * Note that attributes are <I>not</I> matched against, unless otherwise
	   * noted.
	   * @param n The node to match against
	   * @return true if they match
	   */
	    public boolean matches(Node n) {

	        if (!matches(n.getLabel(), getLabel())) return false;
	        if (!matches(n.getAgent(), getAgent())) return false;

	        return true;
	    }

	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(String check, String current) {
	        if (check != null) {
	            if (current == null) {
	                return false;
	            } else {
	                /*
	                  if (n.getLabel().endsWith("*")) {
	                  String realn = n.getLabel().substring(0, n.getLabel().indexOf("*"));
	                  if (! getLabel().startsWith(realn)) return false;
	                  } else if (! getLabel().equalsIgnoreCase(n.getLabel())) return false;
	                */
	                //if (! getLabel().equalsIgnoreCase(n.getLabel())) return false;
	                if (! current.equals(check)) return false;
	            }
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(IAgent check, IAgent current) {
	        if (check != null) {
	            if (current == null) {
	                return false;
	            } else {
//	                if (! current.matches(check)) return false;
	            }
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(int check, int current) {
	        if (check != Integer.MIN_VALUE) {
	            if (current != check) return false;
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(long check, long current) {
	        if (check != Long.MIN_VALUE) {
	            if (current != check) return false;
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(float check, float current) {
	        if (check != Float.NEGATIVE_INFINITY) {
	            if (current != check) return false;
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(double check, double current) {
	        if (check != Double.NEGATIVE_INFINITY) {
	            if (current != check) return false;
	        }
	        return true;
	    }
	    /**
	     * Matches helper function.
	     * @see #matches
	     */
	    protected static boolean matches(Boolean check, Boolean current) {
	        if (check != null) {
	            if (current == null) {
	                return false;
	            } else {
	                if (current.booleanValue() != check.booleanValue()) return false;
	            }
	        }
	        return true;
	    }
	    
	    /**
	     * Determines if the node has a particular attribute
	     * @return True if the attribute is present
	     */
	    public boolean hasAttribute(Object k) {
	      return spec_attributes.containsKey(k.toString());
	    }
	    
	    public boolean IsComplete()
		{
			return this.status==COMPLETED;
		}
	    
	    public void MarkCompleted()
		{
			this.status = COMPLETED;
		}
	    
		@Override
		public void NotifyAll() {
			for(Node b : this.Observers)
			{
				b.Update((Node)this);
			}
		}

		@Override
		public void Update(Node observedTask) {
			synchronized(Task.Lock){
			Node matchingTask = null;
			for(Node b: this.children)
			{
				if (b.label.equals(observedTask.label))
				{
					matchingTask = b;
				}
			}
			if (matchingTask!=null)
				this.children.remove(matchingTask);
			if (this.children.isEmpty() && !this.IsComplete())
			{
				MarkCompleted();
			}}
		}

		@Override
		public void AddObserver(Node observer) {
			this.Observers.add(observer);
		}
		
	    
}

