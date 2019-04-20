@Grab(group='net.bioclipse.bacting', module='managers-cdk', version='0.0.3-SNAPSHOT')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

list = cdk.createMoleculeList()
println list
println cdk.fromSMILES("COC")

