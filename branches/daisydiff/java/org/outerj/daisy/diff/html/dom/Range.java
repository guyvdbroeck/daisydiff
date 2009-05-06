package org.outerj.daisy.diff.html.dom;

import org.eclipse.compare.rangedifferencer.RangeDifference;

public class Range implements Comparable<Range>{
	public static final int NOT_DEFINED = -1;
	public static final boolean LEFT = true;
	public static final boolean RIGHT = false;
	
	/**
	 * means the <code>Range</code> ends before something starts
	 */
	public static final int PRECEDES = -2;
	
	/**
	 * means the <code>Range</code> starts after something ends
	 */
	public static final int FOLLOWS = -3;
	
	/**
	 * means the <code>Range</code> starts before something starts,
	 * and ends after something starts, but before it ends
	 */
	public static final int PRECEDES_INTERSECTS = -4;
	
	/**
	 * means the <code>Range</code> starts after something starts, but
	 * before it ends, and ends after that thing ends.
	 */
	public static final int INTERSECTS_FOLLOWS = -5;
	
	/**
	 * means the <code>Range</code> starts before something starts and
	 * ends at the end of that something.
	 */
	public static final int PRECEDES_INCLUDES = -6;
	
	/**
	 * means the <code>Range</code> starts where something starts and 
	 * ends after that something ends.
	 */
	public static final int INCLUDES_FOLLOWS = -7;
	
	/**
	 * means the <code>Range</code> starts before something starts and
	 * ends after something ends (the range is wider than something and
	 * contains that something completely)
	 */
	public static final int CONTAINS = -8;
	
	/**
	 * means the <code>Range</code>'s start and end points match the
	 * start and end points of something (notice, that difference's "end"
	 * point on the element that is not included in the difference, 
	 * while in the range the end points to the last element that is included.
	 * That means that if the difference has start==x and end==y, then 
	 * the range <code>MATCHES</code> it if 
	 * range.start==x and range.end==(y-1)).
	 */
	public static final int MATCHES = -9;
	
	/**
	 * means the <code>Range</code> starts after something starts and
	 * ends before something ends. (means the <code>Range</code> is
	 * narrower than that something and completely inside of that something.
	 */
	public static final int SUBSETS = -10;
	
	
	private int start;
	private int end;
	
	public Range (int start, int end){
		setStart(start);
		setEnd(end);
	}
	
	public Range(int start){
		setStart(start);
		setEnd(NOT_DEFINED);
	}
	
	public int getStart(){
		return start;
	}
	
	public void setStart(int newValue){
		if (newValue < 0){
			this.start = NOT_DEFINED;
		} else {
			this.start = newValue;
		}
	}
	
	public int getEnd(){
		return end;
	}

	public void setEnd(int newValue){
		if (newValue < 0){
			this.end = NOT_DEFINED;
		} else {
			this.end = newValue;
		}
	}
	
	public int getLength(){
		if (this.end == Range.NOT_DEFINED &&
			this.start == Range.NOT_DEFINED){
			return Range.NOT_DEFINED;
		} else {
			return getEnd() + 1 - getStart();
		}
	}
	
	public boolean equals(Object o){
		if (o == null || !(o instanceof Range)){
			return false;
		}
		Range another = (Range)o;
		if (this.getStart() == another.getStart() &&
			this.getEnd() == another.getEnd()){
			return true;
		} else {
			return false;
		}
	}
	
	public int hashCode(){
		int simple = 29;
		return start * simple + end;
	}
	
	@Override
	public int compareTo(Range another) {
		if (another == null){//all null are very last
			return -1;
		}
		int thisStart = this.getStart();
		int anotherStart = another.getStart();
		if (thisStart < anotherStart){
			return -1;
		} else if (anotherStart < thisStart){
			return 1;
		} else {//start is equal
			int thisEnd = this.getEnd();
			int anotherEnd = another.getEnd();
			if (anotherEnd < thisEnd){
				//means another is nested in this
				return -1;
			} else if (thisEnd < anotherEnd){
				//means this is nested in another
				return 1;
			} else {
				//starts and ends are the same - equals.
				return 0;
			}
		}
	}
	
	public String toString(){
		return "[" + this.start + "-" + this.end + "]";
	}
	
	public boolean doesNotContain(RangeDifference diff, boolean left){
		if (diff == null){ 
			return true;
		}
		//on which side are we?
		if (left){
			if (diff.leftEnd() <= this.start || 
				this.end < diff.leftStart()){
				return true;
			} else {
				return false;
			}
		} else {
			if (diff.rightEnd() <= this.start || 
				this.end < diff.rightStart()){
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean doesNotContain(int point){
		return (point < this.start || this.end < point);
	}
	
	public boolean contains(int point){
		return (this.start <= point && point <= this.end);
	}
	
	public boolean contains(Range other){
		return (this.start <= other.getStart() && other.getEnd() <= this.end);
	}
	
	/**
	 * Checks the position of <code>this</code> range relative to 
	 * the provided difference
	 * @param diff - the difference to relate to
	 * @param left - whether <code>this</code> range is on the left side
	 * @return one of the following <code>static</code> constants 
	 * of this class:<code>
	 * <ul><li>public static final int PRECEDES = -2;</li>
	 *     <li>public static final int FOLLOWS = -3;</li>
	 *     <li>public static final int PRECEDES_INTERSECTS = -4;</li>
	 *     <li>public static final int INTERSECTS_FOLLOWS = -5;</li>
	 *     <li>public static final int PRECEDES_INCLUDES = -6;</li>
	 *     <li>public static final int INCLUDES_FOLLOWS = -7;</li>
	 *     <li>public static final int CONTAINS = -8;</li>
	 *     <li>public static final int MATCHES = -9;</li>
	 *     <li>public static final int SUBSETS = -10;</li>
	 * </ul>
	 * </code>
	 * so the phrase would be true: <br>
	 * <code>this</code> range &lt;CONSTANTS_NAME&gt; the <code>diff</code>
	 * (the parameter)
	 * <br>E.g.: if this method returns <code>FOLLOWS</code> constant
	 * it means that this range FOLLOWS the provided difference (so there is
	 * no intersection between the difference and this range).
	 * @throws <code>java.lang.IllegalArgumentException</code> if
	 * the <code>diff</code> parameter is <code>null</code>
	 */
	public int getRelativePosition(RangeDifference diff, boolean left){
		if (diff == null){
			throw new IllegalArgumentException(
					"No null parameters allowed");
		}
		int diffStart = NOT_DEFINED, diffEnd = NOT_DEFINED;
		//on which side are we?
		if (left){
			diffStart = diff.leftStart();
			diffEnd = diff.leftEnd() - 1;
		} else {
			diffStart = diff.rightStart();
			diffEnd = diff.rightEnd() - 1;
		}
		//find out the position
		if (diffStart <= diffEnd){
			//then difference is on our side too
			return getRelativePosition(diffStart, diffEnd);
		} else {			
			//the diff is on the other side
			//while on our side it's just a point in-between of indices
			int totalPosition = 
				getRelativePosition(diffEnd, diffStart);
			switch (totalPosition){
				case PRECEDES:
				case FOLLOWS:
				case CONTAINS:
					return totalPosition;
				case PRECEDES_INCLUDES:
				case INCLUDES_FOLLOWS:
				case MATCHES:
					return CONTAINS;
				case PRECEDES_INTERSECTS:
					return PRECEDES;
				case INTERSECTS_FOLLOWS:
					return FOLLOWS;
				case SUBSETS:
					if (getRelativePosition(diffStart) == MATCHES){
						return FOLLOWS;
					} else {
						return PRECEDES;
					}
				default:
					return NOT_DEFINED;
			}
		}
	}
	
	public int getRelativePosition(Range other){
		return getRelativePosition(other.getStart(), other.getEnd());
	}
	
	public int getRelativePosition(int chkStart, int chkEnd){
		
		if (chkEnd < chkStart || chkStart < 0 || chkEnd < 0){
			return NOT_DEFINED;
		}
		
		//a) 2 cases of no intersection
		if (this.end < chkStart){
			return PRECEDES;
		}
		if (chkEnd < this.start){
			return FOLLOWS;
		}
		
		//b) what kind of intersection?
		if (this.start < chkStart){
			if (this.end < chkEnd){
				return PRECEDES_INTERSECTS;
			} else if (this.end == chkEnd){
				return PRECEDES_INCLUDES;
			} else { //if this.end > diffEnd
				return CONTAINS;
			}
		} else if (this.start == chkStart){
			if (this.end < chkEnd){
				return SUBSETS;
			} else if (this.end == chkEnd){
				return MATCHES;
			} else {//if this.end > diffEnd
				return INCLUDES_FOLLOWS;
			}
		} else {//means this.start > diffStart
			if (this.end <= chkEnd){
				return SUBSETS;
			} else {//means this.end > diffEnd
				return INTERSECTS_FOLLOWS;
			}
		}
	}
	
	public int getRelativePosition(int point){
		return getRelativePosition(point, point);
	}
}
