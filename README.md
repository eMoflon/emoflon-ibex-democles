# emoflon-ibex-democles
Democles-specific component for emoflon-ibex

## Installation

### All Users
0. Install GraphViz http://www.graphviz.org/Download..php
1. Install Gurobi 7.0.2 (make sure it is exactly this version!) http://www.gurobi.com/downloads/gurobi-optimizer
2. Get the latest version of the Eclipse Modeling Tools:  http://www.eclipse.org/downloads/packages/
3. Install the latest version of Xtext: http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/
4. Install Ibex from this update site: https://emoflon.github.io/emoflon-ibex-democles/org.emoflon.ibex.tgg.ide.democles.updatesite/  
Make sure you enable "contact all update sites" in the update manager so all dependencies are automatically installed.

### Developers
5. Check encoding for Xtend Files
  - In Eclipse: Go to ```Window->Preferences->General->Workspace```
  - Change the text file encoding to 'Other: UTF-8'
6. Go to ```File/Import.../Team/Team Project Set```, check URL and enter in and import this PSF file:  https://raw.githubusercontent.com/eMoflon/emoflon-ibex-democles/master/org.emoflon.ibex.tgg.workspace_configuration/devProjectSet.psf
7. Execute MWE2
  - Open project ```org.moflon.ibex.tgg.editor```
  - Go to package ```src/org.moflon.tgg.mosl```
  - Right-click on GenerateTGG.mwe2
  - Press ```Run As -> MWE2 Workflow```
8. Set up your runtime and test workspaces by starting a runtime Eclipse workspace from your dev workspace, and importing this PSF file: https://raw.githubusercontent.com/eMoflon/emoflon-ibex-tests/master/org.emoflon.ibex.tests.workspace_configuration/testProjectSet.psf  
9. Run the JUnit tests to ensure that all is well (everything should be green) by right-clicking ```Testsuite_1.launch``` and selecting ```Run As/JUnit```.
