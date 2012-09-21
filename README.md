glassfish-ondemand-deployment
===============================

offering a deployment mode on which while activating a osgi bundle using getService method on the bundle's activator, if the
bundle's dependencies which provide services and also need to be started, do not exist on current felix cache, the mode will download the
dependencies from remote/local OBR repositories or Maven repositories, then install and start the dependencies. Sometimes this is called
activating-time resolving.