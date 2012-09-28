/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.osgiondemanddeployment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

/**
 * OndemandDeploymentObrHandler(ToDo:)
 * Most of part is copied from ObrHandler Class
 * @author Sahoo(sanjeeb.sahoo@oracle.com)
 * @author Tang Yong(tangyong@cn.fujitsu.com)
 */
public class OndemandDeploymentObrHandler extends ServiceTracker{

	// Now repositories maintain a list which we use during ondemand deployment resolution process.
    // That way, 
	// 1) adding System Repository and Local Repository
	// 2) adding setuped Repositories on HK2 layer's obr Handing
	// 3) adding resolved Repositories from deploy command
    private List<Repository> repositories = new ArrayList<Repository>();
    
    /**
     * No. of milliseconds a thread waits for obtaining a reference to repository admin service before timing out.
     */
    private static final long OBR_TIMEOUT = 10000; // in ms
    
    private static final String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    
    /**
     * List of HK2 module repository URIs. Currently, we only support directory URIs.
     */
    private static final String HK2_REPOSITORIES = "com.sun.enterprise.hk2.repositories";
    
    /**
     * File name prefix used to store generated OBR repository information.
     * This will be suffixed with repository directory name.
     * The file extension will depend on whether we store a binary file or an xml file.
     * For binary file, no extension will be used. For xml file, .xml will be used as extension.
     */
    static final String OBR_FILE_NAME_PREFIX = "obr-";
    
    private BundleContext bundleContext = null;
    
	public OndemandDeploymentObrHandler(BundleContext bctx) {
        super(bctx, RepositoryAdmin.class.getName(), null);
        bundleContext = bctx;
        open();
    }
	
	@Override
    public Object addingService(ServiceReference reference) {
        if (this.getTrackingCount() == 1) {
            return null; // we are not tracking this
        }
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) context.getService(reference);
        repositories.add(repositoryAdmin.getSystemRepository());
        repositories.add(repositoryAdmin.getLocalRepository());
        
        //Tang Yong Added ---> Adding setuped Repositories on HK2 layer's obr Handing
        //Repository File: obr-modules.xml
        ArrayList<Repository> gfRepoList = loadGFSystemRepository(bundleContext.getProperty(HK2_REPOSITORIES));
        if (gfRepoList.size() != 0 ){
           for(Repository repo: gfRepoList){
        	   repositories.add(repo);
           }
        }
        
        return super.addingService(reference);
    }
	
	private ArrayList<Repository> loadGFSystemRepository(String hk2RepositoryUris){
		ArrayList<Repository> list = new ArrayList<Repository>();
	     if (hk2RepositoryUris != null) {
	            for (String s : hk2RepositoryUris.split("\\s")) {
	                URI repoURI = URI.create(s);
	                File repoDir = new File(repoURI);
	                File repoFile = getRepositoryFile(repoDir);
	                try {
						list.add(loadRepository(repoFile));
					} catch (Exception e) {
					}
	            }
	     }
	     
	     return list;
	}

    @Override
    public void remove(ServiceReference reference) {
        super.remove(reference);
    }

    public RepositoryAdmin getRepositoryAdmin() {
        assert (getTrackingCount() < 2);
        try {
            return (RepositoryAdmin) waitForService(OBR_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
    
    public synchronized void addRepository(URI obrUri) throws Exception {
        if (isDirectory(obrUri)) {
            setupRepository(new File(obrUri));
        } else {
            repositories.add(getRepositoryAdmin().getHelper().repository(obrUri.toURL()));
        }
    }
    
    private boolean isDirectory(URI obrUri) {
        try {
            return new File(obrUri).isDirectory();
        } catch (Exception e) {
        }
        return false;
    }
    
    private void setupRepository(final File repoDir) throws Exception {
    	Repository repository;
        File repoFile = getRepositoryFile(repoDir);
        if (repoFile != null && repoFile.exists()) {
            repository = updateRepository(repoFile, repoDir);
        } else {
            repository = createRepository(repoFile, repoDir);
        }
        repositories.add(repository);      
    }
    
    private File getRepositoryFile(File repoDir) {
        String extn = ".xml";
        String cacheDir = context.getProperty(HK2_CACHE_DIR);
        if (cacheDir == null) {
            return null; // caching is disabled, so don't do it.
        }
        return new File(cacheDir, OBR_FILE_NAME_PREFIX + repoDir.getName() + extn);
    }
    
    private Repository updateRepository(File repoFile, File repoDir) throws Exception {
        Repository repository = loadRepository(repoFile);
        if (isObsoleteRepo(repository, repoFile, repoDir)) {
            if (!repoFile.delete()) {
                throw new IOException("Failed to delete " + repoFile.getAbsolutePath());
            }
            repository = createRepository(repoFile, repoDir);
        }
        return repository;
    }
    
    private Repository loadRepository(File repoFile) throws Exception {
        assert (repoFile != null);
        return getRepositoryAdmin().getHelper().repository(repoFile.toURI().toURL());
    }
    
    private boolean isObsoleteRepo(Repository repository, File repoFile, File repoDir) {
        // TODO(Sahoo): Revisit this...
        // This method assumes that the cached repoFile has been created before a newer jar is created.
        // So, this method does not always detect stale repoFile. Imagine the following situation:
        // time t1: v1 version of jar is released.
        // time t2: v2 version of jar is released.
        // time t3: repo.xml is populated using v1 version of jar, so repo.xml records a timestamp of t3 > t2.
        // time t4: v2 version of jar is unzipped on modules/ and unzip maintains the timestamp of jar as t2.
        // Next time when we compare timestamp, we will see that repo.xml is newer than this jar, when it is not.
        // So, we include a size check. We go for the total size check...

        long lastModifiedTime = repoFile.lastModified();
        // optimistic: see if the repoDir has been touched. dir timestamp changes when files are added or removed.
        if (repoDir.lastModified() > lastModifiedTime) {
            return true;
        }
        long totalSize = 0;
        // now compare timestamp of each jar and take a sum of size of all jars.
        for (File jar : findAllJars(repoDir)) {
            if (jar.lastModified() > lastModifiedTime) {
                return true;
            }
            totalSize += jar.length();
        }
        // time stamps didn't identify any difference, so check sizes. The probabibility of sizes of all jars being same
        // when some jars have changed is very very low.
        for (Resource r : repository.getResources()) {
            totalSize -= r.getSize();
        }
        if (totalSize != 0) {
            return true;
        }
        return false;
    }
    
    private List<File> findAllJars(File repo) {
        final List<File> files = new ArrayList<File>();
        repo.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    pathname.listFiles(this);
                } else if (pathname.getName().endsWith("jar")) {
                    files.add(pathname);
                }
                return true;
            }
        });
        return files;
    }
    
    /**
     * Create a new Repository from a directory by recurssively traversing all the jar files found there.
     *
     * @param repoFile
     * @param repoDir
     * @return
     * @throws IOException
     */
    private Repository createRepository(File repoFile, File repoDir) throws IOException {
        DataModelHelper dmh = getRepositoryAdmin().getHelper();
        List<Resource> resources = new ArrayList<Resource>();
        for (File jar : findAllJars(repoDir)) {
            Resource r = dmh.createResource(jar.toURI().toURL());
            
            if (r == null) {
            } else {
                resources.add(r);
            }
        }
        Repository repository = dmh.repository(resources.toArray(new Resource[resources.size()]));
        if (repoFile != null) {
            saveRepository(repoFile, repository);
        }
        return repository;
    }
    
    private void saveRepository(File repoFile, Repository repository) throws IOException {
        assert (repoFile != null);
        final FileWriter writer = new FileWriter(repoFile);
        
        getRepositoryAdmin().getHelper().writeRepository(repository, writer);
        writer.flush();
    }
    
    public synchronized Bundle deploy(String name, String version) {
        Resource resource = findResource(name, version);
        if (resource == null) {
            return null;
        }
        if (resource.isLocal()) {
            return getBundle(resource);
        }
        return deploy(resource);
    }
    
    private Bundle getBundle(Resource resource) {
        for (Bundle b : context.getBundles()) {
            final String bsn = b.getSymbolicName();
            final Version bv = b.getVersion();
            final String rsn = resource.getSymbolicName();
            final Version rv = resource.getVersion();
            boolean versionMatching = (rv == bv) || (rv != null && rv.equals(bv));
            boolean nameMatching = (bsn == rsn) || (bsn != null && bsn.equals(rsn));
            if (nameMatching && versionMatching) return b;
        }
        return null;
    }
    
    synchronized Bundle deploy(Resource resource) {
        final Resolver resolver = getRepositoryAdmin().resolver(getRepositories());
        boolean resolved = resolve(resolver, resource);
        if (resolved) {
            resolver.deploy(0);
            return getBundle(resource);
        } else {
            Reason[] reqs = resolver.getUnsatisfiedRequirements();
            return null;
        }
    }
    
    boolean resolve(final Resolver resolver, Resource resource) {
        resolver.add(resource);
        boolean resolved = resolver.resolve();
        return resolved;
    }
    
    private Resource findResource(String name, String version) {
        final RepositoryAdmin repositoryAdmin = getRepositoryAdmin();
        if (repositoryAdmin == null) {
            return null;
        }
        String s1 = "(symbolicname=" + name + ")";
        String s2 = "(version=" + version + ")";
        String query = (version != null) ? "(&" + s1 + s2 + ")" : s1;
        try {
            Resource[] resources = discoverResources(query);
            return resources.length > 0 ? resources[0] : null;
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
    }
    
    private Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
        // TODO(Sahoo): File a bug against Obr to add a suitable method to Repository interface.
        // We can't use the following method, because we can't rely on the RepositoryAdmin to have the correct
        // list of repositories. So, we do the discovery ourselves.
        // return getRepositoryAdmin().discoverResources(query);
        Filter filter = filterExpr != null ? getRepositoryAdmin().getHelper().filter(filterExpr) : null;
        Resource[] resources;
        Repository[] repos = getRepositories();
        List<Resource> matchList = new ArrayList<Resource>();
        for (int repoIdx = 0; (repos != null) && (repoIdx < repos.length); repoIdx++) {
            resources = repos[repoIdx].getResources();
            for (int resIdx = 0; (resources != null) && (resIdx < resources.length); resIdx++) {
                Properties dict = new Properties();
                dict.putAll(resources[resIdx].getProperties());
                if (filter == null || filter.match(dict)) {
                    matchList.add(resources[resIdx]);
                }
            }
        }

        return matchList.toArray(new Resource[matchList.size()]);
    }
    
    private Repository[] getRepositories() {
        return repositories.toArray(new Repository[repositories.size()]);
    }
}
