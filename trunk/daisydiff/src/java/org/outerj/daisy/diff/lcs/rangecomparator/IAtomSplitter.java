package org.outerj.daisy.diff.lcs.rangecomparator;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

public interface IAtomSplitter extends IRangeComparator{

	public Atom getAtom(int i);
	
	public String substring(int startAtom, int endAtom);
	public String substring(int startAtom);
	
}
