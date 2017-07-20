package org.doble.adr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.doble.adr.ADR;
import org.doble.adr.ADRException;
import org.doble.adr.Environment;
import org.junit.*;


import org.hamcrest.CoreMatchers.*;
import org.junit.Assert.*;

import com.google.common.jimfs.*;




public class CommandNewTest {
	private static FileSystem fileSystem;
    final static private String rootPathName = "/project/adr";
    final static private String docsPath = "/docs/adr";
    
    private String[] adrTitles = {"another test architecture decision", 
    		                     "yet another test architecture decision",
    		                     "and still the adrs come", 
    		                     "to be superseded", 
    		                     "some functional name",
    		                     "something to link to", 
    		                     "a very important decision"};
    
    private ADR adr;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Path rootPath = null; 
		
		// Set up the mock file system
		try {
			fileSystem = Jimfs.newFileSystem(Configuration.unix());

			rootPath = fileSystem.getPath("/project");

			Files.createDirectory(rootPath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set up the environment
		adr = new ADR(new Environment.Builder(fileSystem)
				.out(System.out)
				.err(System.err)
				.in(System.in)
				.userDir(rootPathName)
				.editorRunner(new TestEditorRunner())
				.build()
				);
		
		
		// Set up the directory structure
		String[] args = {"init"};
		try {
			adr.run(args);
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}

	
	}

	@After
	public void tearDown() throws Exception {
		fileSystem.close();
	}

	@Test
	public void testSimpleCommand() {
		String adrTitle = "This is a test achitecture decision";
		
		String[] args = TestUtilities.argify("new " + adrTitle);
				
		try {
			adr.run(args);
		} catch (ADRException e) {
			fail("ADR Exception raised");
		}
		
		
		// Check if the ADR file has been created
		assertTrue(Files.exists(fileSystem.getPath("/project/adr/docs/adr/0002-this-is-a-test-achitecture-decision.md"))); // ADR id is 2 as the first ADR was setup during init.
		
	}
	
	@Test
	public void testWithoutTitle() {
		
		// Convert the name to an array of args - including the command.
		String[] args = {"new"};
				
		try {
			adr.run(args);
			fail("No exception raised when the ADR has no title.");
		} catch (ADRException e) {
			assertTrue(true);
		}
		
				
	}
	
	@Test 
	public void testManyADRs() {
		
		// Create a set of test paths, paths of files that should be have been created
		ArrayList<Path> expectedFiles = new ArrayList<Path>();
		ArrayList<String> expectedFileNames = new ArrayList<String>();
		
		for (int id = 0; id < adrTitles.length; id++) {
			String name = TestUtilities.adrFileName(id + 2, adrTitles[id]);
			Path path  = fileSystem.getPath(rootPathName, docsPath, name);
			expectedFiles.add(path);
			expectedFileNames.add(path.toString());
		}
		
		// And now add on the ADR created during initialization
		Path initADR = fileSystem.getPath(rootPathName, docsPath,  "0001-record-architecture-decisions.md");
		expectedFiles.add(initADR);
		expectedFileNames.add(initADR.toString());
		
		
		for (String adrName: adrTitles) {
			// Convert the name to an array of args - including the command.
			String[] args = ("new" + " " + adrName).split(" ");
			try {
				adr.run(args);
			} catch (ADRException e) {
				fail(e.getMessage());
			}
		}
		
		
		// Check to see if the names exist 
		Path docsDir = fileSystem.getPath(rootPathName, docsPath);

		try {
			
			Stream<String> actualFileNamesStream = Files.list(docsDir).map(Path::toString);
			List<String>  actualFileNames = actualFileNamesStream.collect(Collectors.toList());
			   
			assertTrue("File(s) missing", actualFileNames.containsAll(expectedFileNames));
			assertTrue("Unexpected file(s) found", expectedFileNames.containsAll(actualFileNames));
			
		
	
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	
		
	}

}