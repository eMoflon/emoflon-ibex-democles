# emoflon-ibex-democles
Democles-specific component for emoflon-ibex

## Installation

### All Users
0. Install GraphViz http://www.graphviz.org/Download..php
1. Install Gurobi 7.0.2 (make sure it is exactly this version!) http://www.gurobi.com/downloads/gurobi-optimizer
2. Get the latest version of the Eclipse Modeling Tools:  http://www.eclipse.org/downloads/packages/
3. Install Ibex from this update site: https://emoflon.github.io/emoflon-ibex-democles/org.emoflon.ibex.tgg.ide.democles.updatesite/  
    - **Important:** Make sure you enable "contact all update sites" in the update manager so all dependencies are automatically installed.
    - **Important:** After adding the Ibex update site some extra update sites will be added automatically for all dependencies.  Unfortunately, you have to first click on `Manage` and *enable* all these update sites first before clicking `Install`.
    
### Developers
4. Check encoding for Xtend Files
    - In Eclipse: Go to ```Window->Preferences->General->Workspace```
    - Change the text file encoding to 'Other: UTF-8'
5. Go to ```File/Import.../Team/Team Project Set```, check URL and enter in and import this PSF file:  https://raw.githubusercontent.com/eMoflon/emoflon-ibex-democles/master/devProjectSet.psf
6. Execute MWE2
    - Open project ```org.moflon.ibex.tgg.editor```
    - Go to package ```src/org.moflon.tgg.mosl```
    - Right-click on GenerateTGG.mwe2
    - Press ```Run As -> MWE2 Workflow```
7. Set up your runtime and test workspaces by starting a runtime Eclipse workspace from your dev workspace, and importing this PSF file: https://raw.githubusercontent.com/eMoflon/emoflon-ibex-tests/master/testProjectSet.psf  
8. Run the JUnit tests to ensure that all is well (everything should be green) by right-clicking ```Testsuite.launch``` and selecting ```Run As/JUnit```.
