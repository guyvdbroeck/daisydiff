/*
 * Copyright 2011 Carsten Pfeiffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff.html;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Runs all tests in the testdata directory.
 * The directories may be nested until the files <code>a.html</code> and <code>b.html</code> are
 * found, in which case the directory is considered a leaf directory
 * containing actual test data.
 * 
 * A two-way diff will be performed using the file <code>a.html</code> as the "old"
 * version and the file <code>b.html</code> as the "new" version.
 * 
 * Optionally a file <code>ancestor.html</code> may be present, which will be used
 * as the ancestor version in a three-way diff. No two-way diffing will
 * be performed in that case.
 * 
 * The expected results of the two- or three-way diff must be provided
 * in the file <code>expected.xml</code>
 * 
 * A test case fails if the results of the diff does not exactly match the contents
 * of the <code>expected.xml</code> file.
 * 
 * Note: the files are expected to be UTF-8 encoded.
 * 
 * @author Carsten Pfeiffer <carsten.pfeiffer@gebit.de>
 * @version 17 Jun 2011
 */
public class FileBasedTest extends TestCase {
	/**
	 * For creating (missing) test results from current output.
	 */
	private static final boolean CREATE_EXPECTED_RESULTS = false;
	
	/**
	 * For debugging: If empty, all tests residing in the directory will be executed
	 * Otherwise, only tests whose directory name is in this set will be executed.
	 */
	private static final Set<String> ONLY_TESTS = new HashSet<String>();
	static {
//		ONLY_TESTS.add("table-discontinuous-cellcontents-replaced");
	}
	
	@Override
	protected void setUp() throws Exception {
		// ensure that the test runs on platforms with different line endings
		System.setProperty("line.separator", "\n");
		super.setUp();
	}
	
	/**
	 * Runs a file-based test for the given test directory. Inside this
	 * directory, the files a.html, b.html and expected.xml are, optionally
	 * also the file ancestor.html.
	 * @param aTestDir
	 * @throws Exception
	 */
	private void runTest(File aTestDir) throws Exception {
		TestHelper tempHelper = new TestHelper(aTestDir);
		
		String tempResults = null;
		
		if (tempHelper.hasAncestor()) {
			tempResults = HtmlTestFixture.diff(
					tempHelper.readContents(tempHelper.getAncestor()), 
					tempHelper.readContents(tempHelper.getOld()), 
					tempHelper.readContents(tempHelper.getNew()));
		} else {
			tempResults = HtmlTestFixture.diff(
					tempHelper.readContents(tempHelper.getOld()), 
					tempHelper.readContents(tempHelper.getNew()));
		}
		
		String tempExpected = null;
		try {
			tempExpected = tempHelper.getExpectedResults();
		} catch (FileNotFoundException ex) {
			writeResultsFile(aTestDir, tempResults);
			if (CREATE_EXPECTED_RESULTS) {
				// create all missing results instead of bailing out
				return;
			}
			throw ex;
		}
				
		if (!tempExpected.equals(tempResults)) {
			System.out.println("Length: expected: " + tempExpected.length() + ", actual: " + tempResults.length());
			writeResultsFile(aTestDir, tempResults);
			System.err.println("Expected:");
			System.err.println(tempExpected);
			System.err.println("Actual:");
			System.err.println(tempResults);
		}
		assertEquals(tempExpected, tempResults);
	}

	/**
	 * Writes the given contents into a file of the given directory. By default,
	 * the file is named _actual_result.html. This is useful for generating initial
	 * testresults (expected.xml)
	 * @param aDirectory
	 * @param someContents
	 */
	private void writeResultsFile(File aDirectory, String someContents) {
		String tempName = CREATE_EXPECTED_RESULTS ? TestHelper.EXPECTED_NAME : "_actual_result.html";
		System.err.println("Results file " + TestHelper.EXPECTED_NAME + " not found for " + aDirectory.getAbsolutePath() + ", writing actual diff to " + tempName);
		try {
			Writer tempWriter = new OutputStreamWriter(new FileOutputStream(new File(aDirectory, tempName)), TestHelper.ENCODING);
			tempWriter.write(someContents);
			tempWriter.flush();
			tempWriter.close();
		} catch (Exception ex) {
			//ignore, this is just for easier testresults creation
		}
	}

	@Test
	public void testFileBased() throws Throwable {
		File tempRootDir = new File("testdata");
		
		List<File> tempTestDataDirs = findAllTestDataDirs(tempRootDir);
		
		for (File tempDir : tempTestDataDirs) {
			testDir(tempDir);
		}
	}
	
	/**
	 * Returns a list of all directories which contain testdata, that is, a.html, b.html etc.
	 * @param aRootDir the directory to start with
	 */
	private List<File> findAllTestDataDirs(File aRootDir) {
		final List<File> tempIntermediateDirs = new ArrayList<File>();
		
		File[] tempTestDataDirs = aRootDir.listFiles(new FileFilter() {
			
			public boolean accept(File aFile) {
				if (!aFile.isDirectory() || aFile.getName().startsWith(".")) {
					return false;
				}

				if (TestHelper.isTestDataDir(aFile)) {
					if (ONLY_TESTS.isEmpty()) {
						return true;
					}
					return ONLY_TESTS.contains(aFile.getName());
				} else {
					// must be an intermediate directory
					tempIntermediateDirs.add(aFile);
				}
				
				return false;
			}
		});
		
		// recursively find all further test data directories
		List<File> tempResult = new ArrayList<File>(Arrays.asList(tempTestDataDirs));
		for (File tempIntermediate : tempIntermediateDirs) {
			tempResult.addAll(findAllTestDataDirs(tempIntermediate));
		}
		if (ONLY_TESTS.isEmpty() && tempIntermediateDirs.size() > 0 && tempResult.size() == tempTestDataDirs.length) {
			// we had intermediate dirs, but they did not contain any test data directories: fail
			StringBuilder tempBuilder = new StringBuilder();
			for (File tempFile : tempIntermediateDirs) {
				tempBuilder.append(tempFile.getAbsolutePath()).append("\n");
			}
			
			fail("Directories without testdata found: " + tempBuilder.toString());
		}
		
		return tempResult;
	}

	private void testDir(File aDir) throws Throwable {
		try {
			runTest(aDir);
		} catch (Throwable ex) {
			System.err.println("Error was in test: " + aDir);
			ex.printStackTrace();
			throw ex;
		}
	}
}
