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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * OnDemandDeploymentBundleContextImpl class intercepts the methods
 * getServiceReferences(), getServiceReference(), getService()
 * 
 * <p>
 * Currently, the OndemandDeploymentBundleContextImpl class is based on OSGi 4.2 API
 * </p>
 *  
 * @author Tang Yong(tangyong@cn.fujitsu.com)
 */
public class OndemandDeploymentBundleContextImpl implements BundleContext {

	protected BundleContext bundleContext;
    private final OndemandDeploymentObrHandler ondemandObrHandler;

	public OndemandDeploymentBundleContextImpl(BundleContext bundleContext) {
	    this.bundleContext = bundleContext;
	    ondemandObrHandler = (OndemandDeploymentObrHandler) bundleContext.getService(bundleContext.getServiceReference("OndemandDeploymentObrHandler"));
	}
	
	public String getProperty(String key) {
		return bundleContext.getProperty(key);
	}

	public Bundle getBundle() {
		return bundleContext.getBundle();
	}

	public Bundle installBundle(String s, InputStream input) throws BundleException {
		return bundleContext.installBundle(s, input);
	}

	public Bundle installBundle(String location) throws BundleException {
		return bundleContext.installBundle(location);
	}
	
	public Bundle getBundle(long id) {
		return bundleContext.getBundle(id);
	}

	public Bundle[] getBundles() {
		return bundleContext.getBundles();
	}

	public void addServiceListener(ServiceListener listener, String filter)
			throws InvalidSyntaxException {
		bundleContext.addServiceListener(listener, filter);
	}

	public void addServiceListener(ServiceListener listener) {
	    bundleContext.addServiceListener(listener);
	}

	public void removeServiceListener(ServiceListener listener) {
		bundleContext.removeServiceListener(listener);
	}

	public void addBundleListener(BundleListener listener) {
		bundleContext.addBundleListener(listener);
	}

	public void removeBundleListener(BundleListener listener) {
		bundleContext.removeBundleListener(listener);
	}

	public void addFrameworkListener(FrameworkListener listener) {
		bundleContext.addFrameworkListener(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
		bundleContext.removeFrameworkListener(listener);
	}

	public ServiceRegistration registerService(String[] clazzes, Object service,
			Dictionary properties) {
		return bundleContext.registerService(clazzes, service, properties);
	}

	public ServiceRegistration registerService(String clazz, Object service,
			Dictionary properties) {
		return bundleContext.registerService(clazz, service, properties);
	}

	public File getDataFile(String filename) {
		return bundleContext.getDataFile(filename);
	}

	public Filter createFilter(String filter) throws InvalidSyntaxException {
		return bundleContext.createFilter(filter);
	}

	public ServiceReference[] getServiceReferences(String clazz, String filter)
			throws InvalidSyntaxException {
		//ToDo:(TangYong)
		return null;
	}

	public ServiceReference getServiceReference(String clazz) {
		//ToDo:(TangYong)
		return null;
	}

	/**
	 * @param clazz
	 * @param filterstr
	 * @return @throws
	 *         InvalidSyntaxException
	 */
	private ServiceReference[] _getServiceReferences(String clazz,
			String filterstr, boolean onlyone) throws InvalidSyntaxException {

		//ToDo:(TangYong)
		return null;
	}

	public Object getService(ServiceReference reference) {
		return _getService(reference);
	}

	/**
	 * if the servicereference is an instance of ServiceOnDemandServiceReferenceImpl
	 * the bundle is installed and started and the service is returned
	 * 
	 * @param servicereference
	 * @return the service
	 */
	private Object _getService(ServiceReference servicereference) {
		//ToDo:(TangYong)
		return null;
	}

	public boolean ungetService(ServiceReference servicereference) {
		//ToDo:(TangYong)
		return false;
	}

	@Override
	public ServiceReference[] getAllServiceReferences(String clazz,
			String filter) throws InvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}
}
