# eMoflon::IBeX Democles
Democles-specific component for [eMoflon::IBeX](https://github.com/eMoflon/emoflon-ibex)

## How to install
1. Install [GraphViz](http://www.graphviz.org/download/).
2. Get the latest version of the [Eclipse Modeling Tools](https://www.eclipse.org/downloads/packages/).
3. Install Xtext from this update site:
	http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/
4. Install eMoflon::IBeX from this update site:
	https://emoflon.github.io/emoflon-ibex-democles/org.emoflon.ibex.tgg.ide.democles.updatesite/
    - **Important**: Make sure you enable "contact all update sites" in the update manager
		so all dependencies are automatically installed.
    - **Important**: After adding the IBex update site some extra update sites
		will be added automatically for all dependencies.
		Unfortunately, you have to first click on `Manage` and *enable* all these update sites first
		before clicking `Install`.

Now you are ready to use eMoflon::IBeX Democles.

5. (Optional) Install [GLPK for Windows](https://sourceforge.net/projects/winglpk/)
	or install GLPK via your package manager (Linux).
6. (Optional) Install [Google OR](https://developers.google.com/optimization/introduction/installing/binary) (not necessary on 64-bit Windows)
7. (Optional) Install [Gurobi](http://www.gurobi.com/downloads/gurobi-optimizer) 7.0.2
	(make sure it is exactly this version!)

Note that Gurobi is only free for academical use (but not for commercial).

## How to develop
1. Do all steps from the *How to install* section above.
2. Check the encoding for Xtend files.
    - In Eclipse: Go to ```Window->Preferences->General->Workspace```.
    - Change the text file encoding to 'Other: UTF-8'.
3. Go to ```File/Import.../Team/Team Project Set```, check URL and enter in and import this PSF file:
	https://raw.githubusercontent.com/eMoflon/emoflon-ibex-democles/master/devProjectSet.psf
4. Execute MWE2
    - Open packages ```org.emoflon.ibex.gt.editor/src/org.emoflon.ibex.gt.editor```
		and ```org.emoflon.ibex.tgg.editor/src/org.moflon.tgg.mosl```.
    - Right-click on ```GenerateGT.mwe2``` in the first package and ```GenerateTGG.mwe2``` in the latter.
    - Press ```Run As -> MWE2 Workflow```.
5. Set up your runtime and test workspaces by starting a runtime Eclipse workspace
	from your development workspace, and importing this PSF file:
	https://raw.githubusercontent.com/eMoflon/emoflon-ibex-tests/master/testProjectSet.psf
6. Set UTF-8 as file encoding for the development workspace
	and build all projects to trigger code generation.
7. Run the JUnit tests to ensure that all is well by right-clicking
	one of the ```Testsuite_*.launch``` in the ```Testsuite``` project
	and ```TestsuiteGT.launch``` in the ```TestsuiteGT``` project
	and start the tests by selecting ```Run As/JUnit```.
	If everything is set up correctly, all tests should be green.

Running ```Testsuite_GLPK.launch``` requires GLPK (see installation step 5).
	
Running ```Testsuite_Gurobi.launch``` requires Gurobi (see installation step 6).

Running ```Testsuite_CBC.launch``` requires Google OR tools (see installation step 7).

```Testsuite_SAT4J.launch``` uses the SAT4J (automatically installed, but the slowest option).  
