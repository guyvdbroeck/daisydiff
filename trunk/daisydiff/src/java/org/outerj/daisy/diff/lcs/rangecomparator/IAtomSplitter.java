package org.outerj.daisy.diff.lcs.rangecomparator;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.lcs.tag.Atom;

public interface IAtomSplitter extends IRangeComparator{

	public Atom getAtom(int i);
	
}
