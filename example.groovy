@Grab(group='net.bioclipse.managers', module='bioclipse-cdk', version='0.0.1-SNAPSHOT')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

list = cdk.createMoleculeList()
println list
println cdk.fromSMILES("COC")

