# emoflon-ibex-democles
Democles-specific component for emoflon-ibex

## Installation
0. Install GraphViz http://www.graphviz.org/Download..php
1. Install Gurobi 7.0.2 (make sure it is exactly this version!) http://www.gurobi.com/downloads/gurobi-optimizer
2. Get the latest version of the Eclipse Modeling Tools:  http://www.eclipse.org/downloads/packages/
3. Install the latest version of Xtext: http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/
4. Install Ibex from this update site: https://emoflon.github.io/emoflon-ibex-democles/org.emoflon.ibex.tgg.ide.democles.updatesite/  
Make sure you enable "contact all update sites" in the update manager so all dependencies are automatically installed.
5. From this repository, import the ```org.emoflon.ibex.tgg.workspace_configuration``` project into your workspace.  Let's refer to this project as our workspace-config from now on.
6. Check encoding for Xtend Files
  - In Eclipse: Go to ```Window->Preferences->General->Workspace```
  - Change the text file encoding to 'Other: UTF-8'
7. Set up your developer workspace
  - Choose the PSF file ```devProjectSet.psf``` in the workspace-config, right-click and select ```Import Project Set...```.
8. Execute MWE2
  - Open project ```org.moflon.ibex.tgg.editor```
  - Go to package ```src/org.moflon.tgg.mosl```
  - Right-click on GenerateTGG.mwe2
  - Press ```Run As -> MWE2 Workflow```
9. Set up your runtime and test workspaces by starting a runtime Eclipse workspace from your dev workspace, and importing this PSF file: https://github.com/eMoflon/emoflon-ibex-tests/blob/master/org.emoflon.ibex.tests.workspace_configuration/testProjectSet.psf  
Run the JUnit tests to ensure that all is well (everything should be green).
