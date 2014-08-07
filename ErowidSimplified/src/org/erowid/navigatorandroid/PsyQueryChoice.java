package org.erowid.navigatorandroid;

import java.util.Comparator;

/**
 * This was implemented solely so the search results could be sorted with compareTo.
 * Referred here: http://www.mkyong.com/java/java-object-sorting-example-comparable-and-comparator/
 * @author quartz
 *
 */
public class PsyQueryChoice implements Comparable<PsyQueryChoice>{
	public int id;
	public String name;
	public String intentName;
	
	public PsyQueryChoice(int id, String name, String intentName)
	{
		super();
		this.id = id;
		this.name = name;
		this.intentName = intentName;
	}
	
	//sorts by accending order of ids
	@Override
	public int compareTo(PsyQueryChoice choice) {
		// TODO Auto-generated method stub
		int choiceId = ((PsyQueryChoice) choice).id;
		
		//ascending order
		return this.id - choiceId;
		
		//descending order
		//return choiceId - this.id;
	}

	public static Comparator<PsyQueryChoice> PsyQueryComparator  = new Comparator<PsyQueryChoice>() {
	
		public int compare(PsyQueryChoice fruit1, PsyQueryChoice fruit2) {
		
		String psyName1 = fruit1.name.toUpperCase();
		String psyName2 = fruit2.name.toUpperCase();
		
		//ascending order
		return psyName1.compareTo(psyName2);
		
		//descending order
		//return psyName2.compareTo(psyName1);
		}

	};
	
}
